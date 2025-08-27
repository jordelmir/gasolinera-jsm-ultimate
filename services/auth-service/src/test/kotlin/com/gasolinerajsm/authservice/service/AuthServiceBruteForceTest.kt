package com.gasolinerajsm.authservice.service

import com.gasolinerajsm.authservice.config.OtpProperties
import com.gasolinerajsm.authservice.security.SecurityAuditLogger
import com.gasolinerajsm.authservice.validation.PhoneNumberValidator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit

class AuthServiceBruteForceTest {

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var jwtService: JwtService
    private lateinit var userService: UserService
    private lateinit var otpProperties: OtpProperties
    private lateinit var phoneValidator: PhoneNumberValidator
    private lateinit var securityAuditLogger: SecurityAuditLogger
    private lateinit var authService: AuthService
    private lateinit var valueOperations: ValueOperations<String, String>

    @BeforeEach
    fun setUp() {
        redisTemplate = mock()
        jwtService = mock()
        userService = mock()
        securityAuditLogger = mock()
        valueOperations = mock()

        otpProperties = OtpProperties().apply {
            length = 6
            minValue = 100000
            maxValue = 999999
            expirationMinutes = 5
            maxAttempts = 3 // Lower for testing
            lockoutMinutes = 15
        }

        phoneValidator = PhoneNumberValidator()

        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(redisTemplate.hasKey(any())).thenReturn(false)

        authService = AuthService(redisTemplate, jwtService, userService, otpProperties, phoneValidator, securityAuditLogger)
    }

    @Test
    fun `should track failed OTP attempts`() {
        val phone = "+50688888888"
        val wrongCode = "000000"
        val correctCode = "123456"

        // Setup: OTP exists but attempts will fail
        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn(null, "1", "2")
        whenever(valueOperations.get("otp:$phone")).thenReturn(correctCode)
        whenever(redisTemplate.hasKey("otp_used:$phone")).thenReturn(false)

        // First failed attempt
        assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, wrongCode)
        }

        verify(valueOperations).set("otp_attempts:$phone", "1", otpProperties.lockoutMinutes, TimeUnit.MINUTES)
        verify(securityAuditLogger).logOtpVerificationFailed(phone, 1)

        // Second failed attempt
        assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, wrongCode)
        }

        verify(valueOperations).set("otp_attempts:$phone", "2", otpProperties.lockoutMinutes, TimeUnit.MINUTES)
        verify(securityAuditLogger).logOtpVerificationFailed(phone, 2)

        // Third failed attempt
        assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, wrongCode)
        }

        verify(valueOperations).set("otp_attempts:$phone", "3", otpProperties.lockoutMinutes, TimeUnit.MINUTES)
        verify(securityAuditLogger).logOtpVerificationFailed(phone, 3)
    }

    @Test
    fun `should lock account after max attempts`() {
        val phone = "+50688888888"
        val wrongCode = "000000"

        // Setup: Already at max attempts
        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn(otpProperties.maxAttempts.toString())

        val exception = assertThrows(IllegalStateException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, wrongCode)
        }

        assertTrue(exception.message!!.contains("Too many failed attempts"))
        assertTrue(exception.message!!.contains("${otpProperties.lockoutMinutes} minutes"))
        verify(securityAuditLogger).logAccountLocked(phone)

        // Should not increment attempts further or check OTP
        verify(valueOperations, never()).get("otp:$phone")
        verify(valueOperations, never()).set(eq("otp_attempts:$phone"), any(), any<Long>(), any())
    }

    @Test
    fun `should reset attempts counter on successful verification`() {
        val phone = "+50688888888"
        val correctCode = "123456"

        // Setup: Has some failed attempts but OTP is correct
        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn("2")
        whenever(valueOperations.get("otp:$phone")).thenReturn(correctCode)
        whenever(redisTemplate.hasKey("otp_used:$phone")).thenReturn(false)

        val mockUser = mock<Any> {
            on { toString() } doReturn "User(id=123)"
        }
        whenever(userService.findOrCreateUser(phone)).thenReturn(mockUser)
        whenever(jwtService.generateAccessToken(any())).thenReturn("access-token")
        whenever(jwtService.generateRefreshToken(any())).thenReturn("refresh-token")

        authService.verifyOtpAndIssueTokens(phone, correctCode)

        // Should delete attempts counter on success
        verify(redisTemplate).delete("otp_attempts:$phone")
        verify(securityAuditLogger).logOtpVerificationSuccess(phone, any())
        verify(securityAuditLogger, never()).logOtpVerificationFailed(any(), any())
    }

    @Test
    fun `should prevent OTP reuse with single-use protection`() {
        val phone = "+50688888888"
        val correctCode = "123456"

        // Setup: OTP exists and is correct, but already used
        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn(null)
        whenever(valueOperations.get("otp:$phone")).thenReturn(correctCode)
        whenever(redisTemplate.hasKey("otp_used:$phone")).thenReturn(true)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, correctCode)
        }

        assertEquals("OTP has already been used", exception.message)
        verify(securityAuditLogger).logOtpVerificationFailed(phone, 1)

        // Should not proceed to user creation or token generation
        verify(userService, never()).findOrCreateUser(any())
        verify(jwtService, never()).generateAccessToken(any())
    }

    @Test
    fun `should mark OTP as used immediately on successful verification`() {
        val phone = "+50688888888"
        val correctCode = "123456"

        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn(null)
        whenever(valueOperations.get("otp:$phone")).thenReturn(correctCode)
        whenever(redisTemplate.hasKey("otp_used:$phone")).thenReturn(false)

        val mockUser = mock<Any> {
            on { toString() } doReturn "User(id=123)"
        }
        whenever(userService.findOrCreateUser(phone)).thenReturn(mockUser)
        whenever(jwtService.generateAccessToken(any())).thenReturn("access-token")
        whenever(jwtService.generateRefreshToken(any())).thenReturn("refresh-token")

        authService.verifyOtpAndIssueTokens(phone, correctCode)

        // Should mark OTP as used immediately
        verify(valueOperations).set("otp_used:$phone", "used", otpProperties.expirationMinutes, TimeUnit.MINUTES)

        // Should clean up after success
        verify(redisTemplate).delete("otp:$phone")
        verify(redisTemplate).delete("otp_attempts:$phone")
    }

    @Test
    fun `should handle concurrent verification attempts safely`() {
        val phone = "+50688888888"
        val correctCode = "123456"

        // Simulate concurrent access where OTP gets marked as used between checks
        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn(null)
        whenever(valueOperations.get("otp:$phone")).thenReturn(correctCode)
        whenever(redisTemplate.hasKey("otp_used:$phone")).thenReturn(false, true) // First call false, second true

        val mockUser = mock<Any> {
            on { toString() } doReturn "User(id=123)"
        }
        whenever(userService.findOrCreateUser(phone)).thenReturn(mockUser)
        whenever(jwtService.generateAccessToken(any())).thenReturn("access-token")
        whenever(jwtService.generateRefreshToken(any())).thenReturn("refresh-token")

        // First verification should succeed
        authService.verifyOtpAndIssueTokens(phone, correctCode)

        // Second verification should fail due to OTP already being used
        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, correctCode)
        }

        assertEquals("OTP has already been used", exception.message)
    }

    @Test
    fun `should handle invalid OTP with proper attempt tracking`() {
        val phone = "+50688888888"
        val wrongCode = "000000"
        val correctCode = "123456"

        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn("1")
        whenever(valueOperations.get("otp:$phone")).thenReturn(correctCode)
        whenever(redisTemplate.hasKey("otp_used:$phone")).thenReturn(false)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, wrongCode)
        }

        assertEquals("Invalid or expired OTP", exception.message)
        verify(valueOperations).set("otp_attempts:$phone", "2", otpProperties.lockoutMinutes, TimeUnit.MINUTES)
        verify(securityAuditLogger).logOtpVerificationFailed(phone, 2)
    }

    @Test
    fun `should handle expired OTP with proper attempt tracking`() {
        val phone = "+50688888888"
        val code = "123456"

        // OTP doesn't exist (expired)
        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn(null)
        whenever(valueOperations.get("otp:$phone")).thenReturn(null)
        whenever(redisTemplate.hasKey("otp_used:$phone")).thenReturn(false)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, code)
        }

        assertEquals("Invalid or expired OTP", exception.message)
        verify(valueOperations).set("otp_attempts:$phone", "1", otpProperties.lockoutMinutes, TimeUnit.MINUTES)
        verify(securityAuditLogger).logOtpVerificationFailed(phone, 1)
    }

    @Test
    fun `should use configurable lockout duration`() {
        val phone = "+50688888888"
        val wrongCode = "000000"

        // Custom lockout duration
        otpProperties.lockoutMinutes = 30

        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn(null)
        whenever(valueOperations.get("otp:$phone")).thenReturn("123456")
        whenever(redisTemplate.hasKey("otp_used:$phone")).thenReturn(false)

        assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, wrongCode)
        }

        verify(valueOperations).set("otp_attempts:$phone", "1", 30L, TimeUnit.MINUTES)
    }

    @Test
    fun `should use configurable max attempts`() {
        val phone = "+50688888888"
        val wrongCode = "000000"

        // Custom max attempts
        otpProperties.maxAttempts = 2

        // At max attempts
        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn("2")

        val exception = assertThrows(IllegalStateException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, wrongCode)
        }

        assertTrue(exception.message!!.contains("Too many failed attempts"))
        verify(securityAuditLogger).logAccountLocked(phone)
    }
}