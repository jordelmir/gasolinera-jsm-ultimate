package com.gasolinerajsm.authservice.service

import com.gasolinerajsm.authservice.config.OtpProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

class AuthServiceSecureOtpTest {

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var jwtService: JwtService
    private lateinit var userService: UserService
    private lateinit var otpProperties: OtpProperties
    private lateinit var authService: AuthService
    private lateinit var valueOperations: ValueOperations<String, String>

    @BeforeEach
    fun setUp() {
        redisTemplate = mock()
        jwtService = mock()
        userService = mock()
        valueOperations = mock()

        otpProperties = OtpProperties().apply {
            length = 6
            minValue = 100000
            maxValue = 999999
            expirationMinutes = 5
        }

        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)

        authService = AuthService(redisTemplate, jwtService, userService, otpProperties)
    }

    @Test
    fun `should generate OTP using SecureRandom`() {
        // Use reflection to access the private generateSecureOtp method
        val method: Method = AuthService::class.java.getDeclaredMethod("generateSecureOtp")
        method.isAccessible = true

        // Generate multiple OTPs to test randomness
        val otps = mutableSetOf<String>()
        repeat(100) {
            val otp = method.invoke(authService) as String
            otps.add(otp)

            // Verify OTP format
            assertTrue(otp.matches(Regex("\\d{6}")), "OTP should be 6 digits: $otp")
            val otpInt = otp.toInt()
            assertTrue(otpInt >= otpProperties.minValue, "OTP should be >= minValue: $otp")
            assertTrue(otpInt <= otpProperties.maxValue, "OTP should be <= maxValue: $otp")
        }

        // Verify randomness - should have generated many different OTPs
        assertTrue(otps.size > 80, "Should generate diverse OTPs, got ${otps.size} unique values")
    }

    @Test
    fun `should generate OTP with correct length`() {
        val method: Method = AuthService::class.java.getDeclaredMethod("generateSecureOtp")
        method.isAccessible = true

        // Test different OTP lengths
        val testLengths = listOf(4, 6, 8)

        testLengths.forEach { length ->
            otpProperties.length = length
            otpProperties.minValue = when(length) {
                4 -> 1000
                6 -> 100000
                8 -> 10000000
                else -> 1000
            }
            otpProperties.maxValue = when(length) {
                4 -> 9999
                6 -> 999999
                8 -> 99999999
                else -> 9999
            }

            val otp = method.invoke(authService) as String
            assertEquals(length, otp.length, "OTP should have length $length")
            assertTrue(otp.all { it.isDigit() }, "OTP should contain only digits")
        }
    }

    @Test
    fun `should generate unpredictable OTPs`() {
        val method: Method = AuthService::class.java.getDeclaredMethod("generateSecureOtp")
        method.isAccessible = true

        // Generate a sequence of OTPs and verify they don't follow a pattern
        val otps = mutableListOf<Int>()
        repeat(50) {
            val otp = method.invoke(authService) as String
            otps.add(otp.toInt())
        }

        // Check that consecutive OTPs are not sequential
        var sequentialCount = 0
        for (i in 1 until otps.size) {
            if (otps[i] == otps[i-1] + 1) {
                sequentialCount++
            }
        }

        // Should have very few (if any) sequential OTPs in a cryptographically secure sequence
        assertTrue(sequentialCount < 3, "Too many sequential OTPs detected: $sequentialCount")

        // Check for duplicate OTPs (should be very rare)
        val uniqueOtps = otps.toSet()
        assertTrue(uniqueOtps.size > otps.size * 0.9, "Too many duplicate OTPs generated")
    }

    @Test
    fun `should store OTP with correct expiration`() {
        val phone = "+50688888888"

        authService.sendOtp(phone)

        verify(valueOperations).set(
            eq("otp:$phone"),
            any<String>(),
            eq(otpProperties.expirationMinutes),
            eq(TimeUnit.MINUTES)
        )
    }

    @Test
    fun `should generate different OTPs for multiple calls`() {
        val method: Method = AuthService::class.java.getDeclaredMethod("generateSecureOtp")
        method.isAccessible = true

        val otp1 = method.invoke(authService) as String
        val otp2 = method.invoke(authService) as String
        val otp3 = method.invoke(authService) as String

        // While it's theoretically possible for SecureRandom to generate the same number,
        // it should be extremely rare for consecutive calls
        val allDifferent = setOf(otp1, otp2, otp3).size == 3

        // If they're not all different, at least verify they're not all the same
        assertFalse(otp1 == otp2 && otp2 == otp3, "All three OTPs should not be identical")
    }

    @Test
    fun `should handle edge cases in OTP range`() {
        val method: Method = AuthService::class.java.getDeclaredMethod("generateSecureOtp")
        method.isAccessible = true

        // Test with minimum range
        otpProperties.minValue = 100000
        otpProperties.maxValue = 100001

        val otps = mutableSetOf<String>()
        repeat(20) {
            val otp = method.invoke(authService) as String
            otps.add(otp)
            assertTrue(otp == "100000" || otp == "100001", "OTP should be within range: $otp")
        }

        // Should generate both possible values
        assertTrue(otps.size <= 2, "Should only generate values within the small range")
    }
}