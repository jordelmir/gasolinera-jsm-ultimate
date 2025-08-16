package com.gasolinerajsm.authservice.controller

import com.gasolinerajsm.authservice.dto.OtpRequest
import com.gasolinerajsm.authservice.dto.OtpVerifyRequest
import com.gasolinerajsm.authservice.dto.TokenResponse
import com.gasolinerajsm.authservice.service.JwtService
import com.gasolinerajsm.authservice.service.UserService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/auth")
class AuthController(
    private val jwtService: JwtService,
    private val redisTemplate: StringRedisTemplate,
    private val userService: UserService
) {

    @PostMapping("/otp/request")
    fun requestOtp(@RequestBody request: OtpRequest): ResponseEntity<Void> {
        val otpCode = (100000..999999).random().toString() // Generate 6-digit code
        redisTemplate.opsForValue().set(request.phone, otpCode, 5, TimeUnit.MINUTES) // Store for 5 mins
        println("OTP for ${request.phone}: $otpCode") // For manual testing
        return ResponseEntity.ok().build()
    }

    @PostMapping("/otp/verify")
    fun verifyOtp(@RequestBody request: OtpVerifyRequest): ResponseEntity<Any> {
        val storedOtp = redisTemplate.opsForValue().get(request.phone)

        if (storedOtp == null || storedOtp != request.code) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Invalid OTP"))
        }

        // OTP is valid, remove it from Redis to prevent reuse
        redisTemplate.delete(request.phone)

        val user = userService.findOrCreateUser(request.phone)
        val accessToken = jwtService.generateAccessToken(user.id)
        val refreshToken = jwtService.generateRefreshToken(user.id)

        return ResponseEntity.ok(TokenResponse(accessToken, refreshToken))
    }
}
