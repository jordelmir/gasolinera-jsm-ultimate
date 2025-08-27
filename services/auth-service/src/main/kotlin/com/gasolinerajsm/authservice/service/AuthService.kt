package com.gasolinerajsm.authservice.service

import com.gasolinerajsm.authservice.config.OtpProperties
import com.gasolinerajsm.authservice.dto.TokenResponse
import com.gasolinerajsm.authservice.security.SecurityAuditLogger
import com.gasolinerajsm.authservice.validation.PhoneNumberValidator
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

/**
 * Service responsible for authentication operations including OTP generation,
 * verification, and token issuance.
 *
 * This service follows hexagonal architecture principles by depending on
 * abstractions (ports) rather than concrete implementations.
 */
@Service
class AuthService(
    private val redisTemplate: StringRedisTemplate,
    private val jwtService: JwtService,
    private val userService: UserService,
    private val otpProperties: OtpProperties,
    private val phoneValidator: PhoneNumberValidator,
    private val securityAuditLogger: SecurityAuditLogger
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    companion object {
        private const val OTP_PREFIX = "otp:"
        private const val OTP_ATTEMPTS_PREFIX = "otp_attempts:"
        private const val OTP_USED_PREFIX = "otp_used:"
    }

    private val secureRandom = SecureRandom()

    /**
     * Generates and stores an OTP for the given phone number.
     * In production, this should trigger SMS sending.
     *
     * @param phone The phone number to send OTP to (supports E.164 and Costa Rican formats)
     * @throws IllegalArgumentException if phone number is invalid
     */
    fun sendOtp(phone: String) {
        // Validate and normalize phone number
        val validationResult = phoneValidator.validate(phone)
        if (!validationResult.isValid) {
            securityAuditLogger.logInvalidPhoneFormat(phone)
            throw IllegalArgumentException(validationResult.errorMessage ?: "Invalid phone number format")
        }

        val normalizedPhone = validationResult.normalizedNumber!!
        val otp = generateSecureOtp()
        val redisKey = "$OTP_PREFIX$normalizedPhone"

        // Clear any existing OTP for this phone number
        redisTemplate.delete(redisKey)
        redisTemplate.delete("$OTP_USED_PREFIX$normalizedPhone")

        redisTemplate.opsForValue().set(
            redisKey,
            otp,
            otpProperties.expirationMinutes,
            TimeUnit.MINUTES
        )

        securityAuditLogger.logOtpGenerated(normalizedPhone)
        logger.info("Generated OTP for phone number")

        // TODO: Replace with actual SMS service integration
        // otpSender.send(normalizedPhone, "Your Gasolinera JSM code is: $otp")
    }

    /**
     * Verifies the provided OTP and issues JWT tokens if valid.
     * Implements rate limiting to prevent brute-force attacks.
     *
     * @param phone The phone number associated with the OTP
     * @param code The OTP code to verify
     * @return TokenResponse containing access and refresh tokens
     * @throws IllegalArgumentException if OTP is invalid or expired
     * @throws IllegalStateException if too many failed attempts
     */
    fun verifyOtpAndIssueTokens(phone: String, code: String): TokenResponse {
        // Validate and normalize phone number
        val validationResult = phoneValidator.validate(phone)
        if (!validationResult.isValid) {
            securityAuditLogger.logInvalidPhoneFormat(phone)
            throw IllegalArgumentException(validationResult.errorMessage ?: "Invalid phone number format")
        }

        val normalizedPhone = validationResult.normalizedNumber!!

        require(code.isNotBlank() && code.length == otpProperties.length) {
            "OTP code must be ${otpProperties.length} digits"
        }

        // Check for rate limiting
        val attemptsKey = "$OTP_ATTEMPTS_PREFIX$normalizedPhone"
        val attempts = redisTemplate.opsForValue().get(attemptsKey)?.toIntOrNull() ?: 0

        if (attempts >= otpProperties.maxAttempts) {
            securityAuditLogger.logAccountLocked(normalizedPhone)
            throw IllegalStateException(
                "Too many failed attempts. Please try again in ${otpProperties.lockoutMinutes} minutes."
            )
        }

        val redisKey = "$OTP_PREFIX$normalizedPhone"
        val usedKey = "$OTP_USED_PREFIX$normalizedPhone"
        val storedOtp = redisTemplate.opsForValue().get(redisKey)

        // Check if OTP was already used (single-use protection)
        if (redisTemplate.hasKey(usedKey)) {
            securityAuditLogger.logOtpVerificationFailed(normalizedPhone, attempts + 1)
            throw IllegalArgumentException("OTP has already been used")
        }

        if (storedOtp == null || storedOtp != code) {
            // Increment failed attempts
            val newAttempts = attempts + 1
            redisTemplate.opsForValue().set(attemptsKey, newAttempts.toString(), otpProperties.lockoutMinutes, TimeUnit.MINUTES)

            securityAuditLogger.logOtpVerificationFailed(normalizedPhone, newAttempts)
            throw IllegalArgumentException("Invalid or expired OTP")
        }

        // Mark OTP as used immediately to prevent reuse
        redisTemplate.opsForValue().set(usedKey, "used", otpProperties.expirationMinutes, TimeUnit.MINUTES)

        // Find or create user
        val user = userService.findOrCreateUser(normalizedPhone)

        // Generate tokens
        val accessToken = jwtService.generateAccessToken(user.id.toString())
        val refreshToken = jwtService.generateRefreshToken(user.id.toString())

        // Clean up OTP and attempts counter on success
        redisTemplate.delete(redisKey)
        redisTemplate.delete(attemptsKey)

        securityAuditLogger.logOtpVerificationSuccess(normalizedPhone, user.id.toString())
        securityAuditLogger.logSuccessfulAuthentication(user.id.toString(), normalizedPhone)

        return TokenResponse(accessToken, refreshToken)
    }

    /**
     * Generates a cryptographically secure OTP using SecureRandom.
     * This ensures the OTP is unpredictable and secure against attacks.
     *
     * @return A secure OTP as string with configured length
     */
    private fun generateSecureOtp(): String {
        val otp = secureRandom.nextInt(otpProperties.minValue, otpProperties.maxValue + 1)
        return otp.toString()
    }

    /**
     * Revokes a JWT token by adding it to the blacklist.
     * This allows immediate invalidation of compromised tokens.
     *
     * @param token The JWT token to revoke
     */
    fun revokeToken(token: String) {
        jwtService.blacklistToken(token)
        securityAuditLogger.logTokenRevoked(token)
        logger.info("Token revoked successfully")
    }
}