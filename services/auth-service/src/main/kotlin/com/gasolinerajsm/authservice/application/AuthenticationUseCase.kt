package com.gasolinerajsm.authservice.application

import com.gasolinerajsm.authservice.domain.PhoneNumber
import com.gasolinerajsm.authservice.domain.User
import com.gasolinerajsm.authservice.domain.ports.OtpService
import com.gasolinerajsm.authservice.domain.ports.TokenService
import com.gasolinerajsm.authservice.domain.ports.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Use case for authentication operations.
 * This contains the business logic for OTP-based authentication.
 */
@Service
class AuthenticationUseCase(
    private val userRepository: UserRepository,
    private val otpService: OtpService,
    private val tokenService: TokenService
) {

    private val logger = LoggerFactory.getLogger(AuthenticationUseCase::class.java)

    /**
     * Request OTP for phone number authentication
     */
    fun requestOtp(phoneNumber: PhoneNumber): RequestOtpResult {
        logger.info("OTP requested for phone number ending in {}", phoneNumber.value.takeLast(4))

        val otp = otpService.generateAndStore(phoneNumber)

        // In production, this would trigger SMS sending
        logger.debug("Generated OTP for {}: {}", phoneNumber.value, otp)

        return RequestOtpResult.Success
    }

    /**
     * Verify OTP and authenticate user
     */
    fun verifyOtpAndAuthenticate(phoneNumber: PhoneNumber, otp: String): VerifyOtpResult {
        logger.info("OTP verification requested for phone number ending in {}", phoneNumber.value.takeLast(4))

        if (!otpService.verify(phoneNumber, otp)) {
            logger.warn("Invalid OTP attempt for phone number ending in {}", phoneNumber.value.takeLast(4))
            return VerifyOtpResult.InvalidOtp
        }

        // Find or create user
        val user = userRepository.findByPhoneNumber(phoneNumber)
            ?: userRepository.save(User.create(phoneNumber))

        // Generate tokens
        val accessToken = tokenService.generateAccessToken(user.id, user.roles)
        val refreshToken = tokenService.generateRefreshToken(user.id)

        // Clean up OTP
        otpService.remove(phoneNumber)

        logger.info("Successfully authenticated user with ID: {}", user.id)

        return VerifyOtpResult.Success(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = user.id
        )
    }
}

/**
 * Result types for authentication operations
 */
sealed class RequestOtpResult {
    object Success : RequestOtpResult()
    data class Error(val message: String) : RequestOtpResult()
}

sealed class VerifyOtpResult {
    data class Success(
        val accessToken: String,
        val refreshToken: String,
        val userId: com.gasolinerajsm.authservice.domain.UserId
    ) : VerifyOtpResult()

    object InvalidOtp : VerifyOtpResult()
    data class Error(val message: String) : VerifyOtpResult()
}