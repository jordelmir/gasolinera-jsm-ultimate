
package com.gasolinerajsm.authservice.service

import com.gasolinerajsm.authservice.controller.TokenResponse
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

    fun sendOtp(phone: String) {
        val otp = Random.nextInt(100000, 999999).toString()
        redisTemplate.opsForValue().set("otp:$phone", otp, 5, TimeUnit.MINUTES)
        
        // In a real app, use the OtpSender interface to send the SMS
        // otpSender.send(phone, "Your Gasolinera JSM code is: $otp")
        println("OTP for $phone: $otp") // Mock implementation
    }

    fun verifyOtpAndIssueTokens(phone: String, code: String): TokenResponse {
        val storedOtp = redisTemplate.opsForValue().get("otp:$phone")
        if (storedOtp == null || storedOtp != code) {
            throw IllegalArgumentException("Invalid or expired OTP")
        }

        // val user = userRepository.findByPhone(phone) ?: userRepository.save(User(phone = phone))
        val userId = "user-placeholder-id" // Replace with actual user ID
        val roles = listOf("USER")

        val accessToken = jwtTokenProvider.createAccessToken(userId, roles)
        val refreshToken = jwtTokenProvider.createRefreshToken(userId)

        redisTemplate.delete("otp:$phone")

        return TokenResponse(accessToken, refreshToken)
    }
}
