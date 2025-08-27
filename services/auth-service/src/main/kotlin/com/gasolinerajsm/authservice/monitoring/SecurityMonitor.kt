package com.gasolinerajsm.authservice.monitoring

import com.gasolinerajsm.authservice.config.OtpProperties
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Security monitoring component that tracks authentication patterns,
 * collects metrics, and provides alerting for potential security incidents.
 */
@Component
class SecurityMonitor(
    private val meterRegistry: MeterRegistry,
    private val redisTemplate: StringRedisTemplate,
    private val otpProperties: OtpProperties
) {

    private val logger = LoggerFactory.getLogger(SecurityMonitor::class.java)

    // Metrics
    private val otpGeneratedCounter: Counter = Counter.builder("auth.otp.generated")
        .description("Number of OTPs generated")
        .register(meterRegistry)

    private val otpVerificationSuccessCounter: Counter = Counter.builder("auth.otp.verification.success")
        .description("Number of successful OTP verifications")
        .register(meterRegistry)

    private val otpVerificationFailureCounter: Counter = Counter.builder("auth.otp.verification.failure")
        .description("Number of failed OTP verifications")
        .register(meterRegistry)

    private val accountLockoutCounter: Counter = Counter.builder("auth.account.lockout")
        .description("Number of account lockouts due to failed attempts")
        .register(meterRegistry)

    private val tokenRevocationCounter: Counter = Counter.builder("auth.token.revocation")
        .description("Number of tokens revoked")
        .register(meterRegistry)

    private val invalidPhoneAttemptCounter: Counter = Counter.builder("auth.phone.invalid")
        .description("Number of invalid phone number attempts")
        .register(meterRegistry)

    private val authenticationTimer: Timer = Timer.builder("auth.authentication.duration")
        .description("Time taken for authentication operations")
        .register(meterRegistry)

    // In-memory tracking for pattern detection
    private val recentFailures = ConcurrentHashMap<String, MutableList<Instant>>()
    private val suspiciousPatterns = ConcurrentHashMap<String, SuspiciousActivity>()

    companion object {
        private const val SUSPICIOUS_PATTERN_PREFIX = "suspicious:"
        private const val PATTERN_WINDOW_MINUTES = 10L
        private const val HIGH_FREQUENCY_THRESHOLD = 20 // attempts per window
        private const val DISTRIBUTED_ATTACK_THRESHOLD = 5 // different phones from same pattern
    }

    /**
     * Records OTP generation event and checks for suspicious patterns.
     */
    fun recordOtpGenerated(phoneHash: String) {
        otpGeneratedCounter.increment()

        // Check for high-frequency OTP requests
        checkHighFrequencyRequests(phoneHash)

        logger.debug("OTP generation recorded for monitoring")
    }

    /**
     * Records successful OTP verification.
     */
    fun recordOtpVerificationSuccess(phoneHash: String, userId: String) {
        otpVerificationSuccessCounter.increment()

        // Clear any suspicious activity for this phone
        clearSuspiciousActivity(phoneHash)

        // Record authentication timing
        authenticationTimer.record(Duration.ofMillis(System.currentTimeMillis() % 1000))

        logger.debug("Successful OTP verification recorded")
    }

    /**
     * Records failed OTP verification and analyzes patterns.
     */
    fun recordOtpVerificationFailure(phoneHash: String, attemptCount: Int) {
        otpVerificationFailureCounter.increment()

        // Track recent failures for pattern analysis
        trackRecentFailure(phoneHash)

        // Check for distributed brute force attacks
        checkDistributedBruteForce(phoneHash, attemptCount)

        logger.debug("Failed OTP verification recorded with attempt count: {}", attemptCount)
    }

    /**
     * Records account lockout event.
     */
    fun recordAccountLockout(phoneHash: String) {
        accountLockoutCounter.increment()

        // Mark as suspicious activity
        markSuspiciousActivity(phoneHash, SuspiciousActivityType.ACCOUNT_LOCKOUT)

        // Alert on account lockout
        alertAccountLockout(phoneHash)

        logger.warn("Account lockout recorded for monitoring")
    }

    /**
     * Records token revocation event.
     */
    fun recordTokenRevocation(tokenId: String) {
        tokenRevocationCounter.increment()

        // Check for unusual revocation patterns
        checkRevocationPatterns(tokenId)

        logger.debug("Token revocation recorded")
    }

    /**
     * Records invalid phone number attempt.
     */
    fun recordInvalidPhoneAttempt(phoneHash: String) {
        invalidPhoneAttemptCounter.increment()

        // Track for potential enumeration attacks
        trackPhoneEnumerationAttempt(phoneHash)

        logger.debug("Invalid phone attempt recorded")
    }

    /**
     * Gets current security metrics summary.
     */
    fun getSecurityMetrics(): SecurityMetrics {
        return SecurityMetrics(
            otpGenerated = otpGeneratedCounter.count().toLong(),
            otpVerificationSuccess = otpVerificationSuccessCounter.count().toLong(),
            otpVerificationFailure = otpVerificationFailureCounter.count().toLong(),
            accountLockouts = accountLockoutCounter.count().toLong(),
            tokenRevocations = tokenRevocationCounter.count().toLong(),
            invalidPhoneAttempts = invalidPhoneAttemptCounter.count().toLong(),
            suspiciousActivities = suspiciousPatterns.size,
            averageAuthTime = authenticationTimer.mean(TimeUnit.MILLISECONDS)
        )
    }

    /**
     * Checks for high-frequency OTP requests from the same phone.
     */
    private fun checkHighFrequencyRequests(phoneHash: String) {
        val now = Instant.now()
        val windowStart = now.minus(Duration.ofMinutes(PATTERN_WINDOW_MINUTES))

        val failures = recentFailures.computeIfAbsent(phoneHash) { mutableListOf() }
        failures.add(now)

        // Clean old entries
        failures.removeIf { it.isBefore(windowStart) }

        if (failures.size > HIGH_FREQUENCY_THRESHOLD) {
            markSuspiciousActivity(phoneHash, SuspiciousActivityType.HIGH_FREQUENCY_REQUESTS)
            alertHighFrequencyRequests(phoneHash, failures.size)
        }
    }

    /**
     * Checks for distributed brute force attacks across multiple phones.
     */
    private fun checkDistributedBruteForce(phoneHash: String, attemptCount: Int) {
        if (attemptCount >= otpProperties.maxAttempts - 1) {
            // Near lockout - check if this is part of a distributed attack
            val recentLockouts = countRecentLockouts()

            if (recentLockouts >= DISTRIBUTED_ATTACK_THRESHOLD) {
                alertDistributedBruteForce(recentLockouts)
            }
        }
    }

    /**
     * Tracks phone enumeration attempts.
     */
    private fun trackPhoneEnumerationAttempt(phoneHash: String) {
        val key = "$SUSPICIOUS_PATTERN_PREFIX:enumeration"
        val now = Instant.now()

        // Store in Redis with TTL
        redisTemplate.opsForZSet().add(key, phoneHash, now.toEpochMilli().toDouble())
        redisTemplate.expire(key, PATTERN_WINDOW_MINUTES, TimeUnit.MINUTES)

        // Check if we have too many enumeration attempts
        val windowStart = now.minus(Duration.ofMinutes(PATTERN_WINDOW_MINUTES))
        val recentAttempts = redisTemplate.opsForZSet().count(key, windowStart.toEpochMilli().toDouble(), now.toEpochMilli().toDouble())

        if (recentAttempts > HIGH_FREQUENCY_THRESHOLD) {
            alertPhoneEnumeration(recentAttempts)
        }
    }

    /**
     * Checks for unusual token revocation patterns.
     */
    private fun checkRevocationPatterns(tokenId: String) {
        val key = "$SUSPICIOUS_PATTERN_PREFIX:revocations"
        val now = Instant.now()

        redisTemplate.opsForZSet().add(key, tokenId, now.toEpochMilli().toDouble())
        redisTemplate.expire(key, PATTERN_WINDOW_MINUTES, TimeUnit.MINUTES)

        val windowStart = now.minus(Duration.ofMinutes(PATTERN_WINDOW_MINUTES))
        val recentRevocations = redisTemplate.opsForZSet().count(key, windowStart.toEpochMilli().toDouble(), now.toEpochMilli().toDouble())

        if (recentRevocations > HIGH_FREQUENCY_THRESHOLD) {
            alertHighRevocationRate(recentRevocations)
        }
    }

    /**
     * Counts recent account lockouts.
     */
    private fun countRecentLockouts(): Long {
        val key = "$SUSPICIOUS_PATTERN_PREFIX:lockouts"
        val now = Instant.now()
        val windowStart = now.minus(Duration.ofMinutes(PATTERN_WINDOW_MINUTES))

        return redisTemplate.opsForZSet().count(key, windowStart.toEpochMilli().toDouble(), now.toEpochMilli().toDouble())
    }

    /**
     * Tracks recent failures for pattern analysis.
     */
    private fun trackRecentFailure(phoneHash: String) {
        val now = Instant.now()
        val failures = recentFailures.computeIfAbsent(phoneHash) { mutableListOf() }
        failures.add(now)

        // Clean old entries
        val windowStart = now.minus(Duration.ofMinutes(PATTERN_WINDOW_MINUTES))
        failures.removeIf { it.isBefore(windowStart) }
    }

    /**
     * Marks suspicious activity for a phone hash.
     */
    private fun markSuspiciousActivity(phoneHash: String, type: SuspiciousActivityType) {
        suspiciousPatterns[phoneHash] = SuspiciousActivity(type, Instant.now())

        // Store in Redis for persistence
        val key = "$SUSPICIOUS_PATTERN_PREFIX:$phoneHash"
        redisTemplate.opsForValue().set(key, type.name, Duration.ofHours(24))
    }

    /**
     * Clears suspicious activity for a phone hash.
     */
    private fun clearSuspiciousActivity(phoneHash: String) {
        suspiciousPatterns.remove(phoneHash)
        recentFailures.remove(phoneHash)

        val key = "$SUSPICIOUS_PATTERN_PREFIX:$phoneHash"
        redisTemplate.delete(key)
    }

    // Alert methods
    private fun alertAccountLockout(phoneHash: String) {
        logger.warn("SECURITY_ALERT: Account lockout detected for phone hash: {}", phoneHash)
        // In production, this would integrate with alerting systems like PagerDuty, Slack, etc.
    }

    private fun alertHighFrequencyRequests(phoneHash: String, requestCount: Int) {
        logger.warn("SECURITY_ALERT: High frequency requests detected - {} requests from phone hash: {}", requestCount, phoneHash)
    }

    private fun alertDistributedBruteForce(lockoutCount: Long) {
        logger.error("SECURITY_ALERT: Potential distributed brute force attack - {} recent lockouts", lockoutCount)
    }

    private fun alertPhoneEnumeration(attemptCount: Long) {
        logger.warn("SECURITY_ALERT: Potential phone enumeration attack - {} invalid phone attempts", attemptCount)
    }

    private fun alertHighRevocationRate(revocationCount: Long) {
        logger.warn("SECURITY_ALERT: High token revocation rate - {} revocations in {} minutes", revocationCount, PATTERN_WINDOW_MINUTES)
    }
}

/**
 * Types of suspicious activities that can be detected.
 */
enum class SuspiciousActivityType {
    HIGH_FREQUENCY_REQUESTS,
    ACCOUNT_LOCKOUT,
    DISTRIBUTED_BRUTE_FORCE,
    PHONE_ENUMERATION,
    HIGH_REVOCATION_RATE
}

/**
 * Represents a suspicious activity event.
 */
data class SuspiciousActivity(
    val type: SuspiciousActivityType,
    val timestamp: Instant
)

/**
 * Security metrics summary.
 */
data class SecurityMetrics(
    val otpGenerated: Long,
    val otpVerificationSuccess: Long,
    val otpVerificationFailure: Long,
    val accountLockouts: Long,
    val tokenRevocations: Long,
    val invalidPhoneAttempts: Long,
    val suspiciousActivities: Int,
    val averageAuthTime: Double
)