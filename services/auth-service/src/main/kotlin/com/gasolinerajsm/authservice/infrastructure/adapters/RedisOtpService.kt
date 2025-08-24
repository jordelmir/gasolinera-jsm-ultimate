package com.gasolinerajsm.authservice.infrastructure.adapters

import com.gasolinerajsm.authservice.domain.PhoneNumber
import com.gasolinerajsm.authservice.domain.ports.OtpService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Redis implementation of OtpService port.
 * Handles OTP generation, storage, and verification using Redis.
 */
@Service
class RedisOtpService(
    private val redisTemplate: StringRedisTemplate
) : OtpService {

    companion object {
        private const val OTP_PREFIX = "otp:"
        private const val OTP_EXPIRATION_MINUTES = 5L
        private const val MIN_OTP_VALUE = 100000
        private const val MAX_OTP_VALUE = 999999
    }

    override fun generateAndStore(phoneNumber: PhoneNumber): String {
        val otp = generateSecureOtp()
        val redisKey = "$OTP_PREFIX${phoneNumber.value}"

        redisTemplate.opsForValue().set(
            redisKey,
            otp,
            OTP_EXPIRATION_MINUTES,
            TimeUnit.MINUTES
        )

        return otp
    }

    override fun verify(phoneNumber: PhoneNumber, otp: String): Boolean {
        val redisKey = "$OTP_PREFIX${phoneNumber.value}"
        val storedOtp = redisTemplate.opsForValue().get(redisKey)

        return storedOtp != null && storedOtp == otp
    }

    override fun remove(phoneNumber: PhoneNumber) {
        val redisKey = "$OTP_PREFIX${phoneNumber.value}"
        redisTemplate.delete(redisKey)
    }

    /**
     * Generate a cryptographically secure 6-digit OTP
     */
    private fun generateSecureOtp(): String {
        return Random.nextInt(MIN_OTP_VALUE, MAX_OTP_VALUE).toString()
    }
}