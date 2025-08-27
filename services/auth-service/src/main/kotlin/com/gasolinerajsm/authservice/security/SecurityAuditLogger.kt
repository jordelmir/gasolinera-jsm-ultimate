package com.gasolinerajsm.authservice.security

import com.gasolinerajsm.authservice.config.OtpProperties
import com.gasolinerajsm.authservice.monitoring.SecurityMonitor
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.Instant

/**
 * Security audit logger that logs authentication events without exposing sensitive data.
 * Uses hashed identifiers and structured logging for security monitoring.
 */
@Component
class SecurityAuditLogger(
    private val otpProperties: OtpProperties,
    private val securityMonitor: SecurityMonitor? = null // Optional to avoid circular dependency
) {

    private val logger = LoggerFactory.getLogger("SECURITY_AUDIT")

    companion object {
        private const val EVENT_TYPE_KEY = "event_type"
        private const val PHONE_HASH_KEY = "phone_hash"
        private const val USER_ID_KEY = "user_id"
        private const val ATTEMPT_COUNT_KEY = "attempt_count"
        private const val TOKEN_ID_KEY = "token_id"
        private const val TIMESTAMP_KEY = "timestamp"
    }

    /**
     * Logs OTP generation event.
     *
     * @param phoneNumber The phone number (will be hashed for logging)
     */
    fun logOtpGenerated(phoneNumber: String) {
otpProperties.enableAuditLogging) return

        val phoneHash = hashPhoneNumber(phoneNumber)

        MDC.put(EVENT_TYPE_KEY, "OTP_GENERATED")
        MDC.put(PHONE_HASH_KEY, phoneHash)
        MDC.put(TIMESTAMP_KEY, Instant.now().toString())

        logger.info("OTP generated for phone hash: {}", phoneHash)

        // Record metrics
        securityMonitor?.recordOtpGenerated(phoneHash)

        MDC.clear()
    }

    /**
     * Logs OTP verification failure.
     *
     * @param phoneNumber The phone number (will be hashed for logging)
     * @param attemptCount Current attempt count for this phone number
     */
    fun logOtpVerificationFailed(phoneNumber: String, attemptCount: Int) {
        if (!otpProperties.enableAuditLogging) return

        val phoneHash = hashPhoneNumber(phoneNumber)

        MDC.put(EVENT_TYPE_KEY, "OTP_VERIFICATION_FAILED")
        MDC.put(PHONE_HASH_KEY, phoneHash)
        MDC.put(ATTEMPT_COUNT_KEY, attemptCount.toString())
        MDC.put(TIMESTAMP_KEY, Instant.now().toString())

        logger.warn("OTP verification failed for phone hash: {} (attempt {})", phoneHash, attemptCount)

        // Record metrics
        securityMonitor?.recordOtpVerificationFailure(phoneHash, attemptCount)

        MDC.clear()
    }

    /**
     * Logs account lockout due to too many failed attempts.
     *
     * @param phoneNumber The phone number (will be hashed for logging)
     */
    fun logAccountLocked(phoneNumber: String) {
        if (!otpProperties.enableAuditLogging) return

        val phoneHash = hashPhoneNumber(phoneNumber)

        MDC.put(EVENT_TYPE_KEY, "ACCOUNT_LOCKED")
        MDC.put(PHONE_HASH_KEY, phoneHash)
        MDC.put(TIMESTAMP_KEY, Instant.now().toString())

        logger.warn("Account locked due to too many failed attempts for phone hash: {}", phoneHash)

        // Record metrics
        securityMonitor?.recordAccountLockout(phoneHash)

        MDC.clear()
    }

    /**
     * Logs JWT token revocation.
     *
     * @param token The JWT token (will extract safe identifier)
     */
    fun logTokenRevoked(token: String) {
        if (!otpProperties.enableAuditLogging) return

        val tokenId = extractTokenId(token)

        MDC.put(EVENT_TYPE_KEY, "TOKEN_REVOKED")
        MDC.put(TOKEN_ID_KEY, tokenId)
        MDC.put(TIMESTAMP_KEY, Instant.now().toString())

        logger.warn("JWT token revoked: {}", tokenId)

        // Record metrics
        securityMonitor?.recordTokenRevocation(tokenId)

        MDC.clear()
    }

    /**
     * Logs successful authentication.
     *
     * @param userId The user ID
     * @param phoneNumber The phone number (will be hashed for logging)
     */
    fun logSuccessfulAuthentication(userId: String, phoneNumber: String) {
        if (!otpProperties.enableAuditLogging) return

        val phoneHash = hashPhoneNumber(phoneNumber)

        MDC.put(EVENT_TYPE_KEY, "AUTHENTICATION_SUCCESS")
        MDC.put(USER_ID_KEY, userId)
        MDC.put(PHONE_HASH_KEY, phoneHash)
        MDC.put(TIMESTAMP_KEY, Instant.now().toString())

        logger.info("Successful authentication for user: {} with phone hash: {}", userId, phoneHash)

        // Record metrics
        securityMonitor?.recordOtpVerificationSuccess(phoneHash, userId)

        MDC.clear()
    }

    /**
     * Logs OTP verification success.
     *
     * @param phoneNumber The phone number (will be hashed for logging)
     * @param userId The user ID
     */
    fun logOtpVerificationSuccess(phoneNumber: String, userId: String) {
        if (!otpProperties.enableAuditLogging) return

        val phoneHash = hashPhoneNumber(phoneNumber)

        MDC.put(EVENT_TYPE_KEY, "OTP_VERIFICATION_SUCCESS")
        MDC.put(PHONE_HASH_KEY, phoneHash)
        MDC.put(USER_ID_KEY, userId)
        MDC.put(TIMESTAMP_KEY, Instant.now().toString())

        logger.info("OTP verification successful for phone hash: {} user: {}", phoneHash, userId)

        MDC.clear()
    }

    /**
     * Logs invalid phone number format attempts.
     *
     * @param invalidPhone The invalid phone number (will be hashed for logging)
     */
    fun logInvalidPhoneFormat(invalidPhone: String) {
        if (!otpProperties.enableAuditLogging) return

        val phoneHash = hashPhoneNumber(invalidPhone)

        MDC.put(EVENT_TYPE_KEY, "INVALID_PHONE_FORMAT")
        MDC.put(PHONE_HASH_KEY, phoneHash)
        MDC.put(TIMESTAMP_KEY, Instant.now().toString())

        logger.warn("Invalid phone number format attempted: {}", phoneHash)

        // Record metrics
        securityMonitor?.recordInvalidPhoneAttempt(phoneHash)

        MDC.clear()
    }

    /**
     * Creates a SHA-256 hash of the phone number for secure logging.
     * Only logs sensitive data if explicitly enabled in configuration.
     *
     * @param phoneNumber The phone number to hash
     * @return SHA-256 hash of the phone number, or masked version if sensitive logging disabled
     */
    private fun hashPhoneNumber(phoneNumber: String): String {
        return if (otpProperties.logSensitiveData) {
            // In development/debug mode, show last 4 digits
            "***${phoneNumber.takeLast(4)}"
        } else {
            // In production, use SHA-256 hash
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(phoneNumber.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }.take(16) // First 16 chars of hash
        }
    }

    /**
     * Extracts a safe identifier from JWT token for logging.
     * Never logs the actual token content.
     *
     * @param token The JWT token
     * @return Safe identifier for logging
     */
    private fun extractTokenId(token: String): String {
        return if (token.length > 20) {
            // Use first 8 and last 8 characters with dots in between
            "${token.take(8)}...${token.takeLast(8)}"
        } else {
            "***TOKEN***"
        }
    }
}