package com.gasolinerajsm.authservice.service

import com.gasolinerajsm.authservice.controller.TokenResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Service
class AuthService(
    private val redisTemplate: StringRedisTemplate,
    private val jwtTokenProvider: JwtTokenProvider
    // private val userRepository: UserRepository,
    // private val otpSender: OtpSender
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun sendOtp(phone: String) {
        val otp = Random.nextInt(100000, 999999).toString()
        redisTemplate.opsForValue().set("otp:$phone", otp, 5, TimeUnit.MINUTES)
        logger.info("Generated OTP for phone number ending in {}", phone.takeLast(4))

        // In a real app, use the OtpSender interface to send the SMS
        // otpSender.send(phone, "Your Gasolinera JSM code is: $otp")
        println("OTP for $phone: $otp") // Mock implementation
    }

    fun verifyOtpAndIssueTokens(phone: String, code: String): TokenResponse {
        val storedOtp = redisTemplate.opsForValue().get("otp:$phone")
        if (storedOtp == null || storedOtp != code) {
            logger.warn("Invalid OTP attempt for phone number ending in {}", phone.takeLast(4))
            throw IllegalArgumentException("Invalid or expired OTP")
        }
        logger.info("Successfully verified OTP for phone number ending in {}", phone.takeLast(4))

        // val user = userRepository.findByPhone(phone) ?: userRepository.save(User(phone = phone))
        val userId = "user-placeholder-id" // Replace with actual user ID
        val roles = listOf("USER")

        val accessToken = jwtTokenProvider.createAccessToken(userId, roles)
        val refreshToken = jwtTokenProvider.createRefreshToken(userId)

        redisTemplate.delete("otp:$phone")
        logger.info("Issued access and refresh tokens for userId: {}", userId)

        return TokenResponse(accessToken, refreshToken)
    }
}