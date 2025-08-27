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

class AuthServicePhoneValidationTest {

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
            maxAttempts = 5
            lockoutMinutes = 15
        }

        phoneValidator = PhoneNumberValidator()

        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(redisTemplate.hasKey(any())).thenReturn(false)

        authService = AuthService(redisTemplate, jwtService, userService, otpProperties, phoneValidator, securityAuditLogger)
    }

    @Test
    fun `should accept valid E164 phone number`() {
        val phone = "+50688888888"

        authService.sendOtp(phone)

        verify(valueOperations).set(
            eq("otp:$phone"),
            any<String>(),
            eq(otpProperties.expirationMinutes),
            eq(TimeUnit.MINUTES)
        )
        verify(securityAuditLogger).logOtpGenerated(phone)
        verify(securityAuditLogger, never()).logInvalidPhoneFormat(any())
    }

    @Test
    fun `should normalize and accept Costa Rican phone with dash`() {
        val phone = "8888-9999"
        val expectedNormalized = "+50688889999"

        authService.sendOtp(phone)

        verify(valueOperations).set(
            eq("otp:$expectedNormalized"),
            any<String>(),
            eq(otpProperties.expirationMinutes),
            eq(TimeUnit.MINUTES)
        )
        verify(securityAuditLogger).logOtpGenerated(expectedNormalized)
        verify(securityAuditLogger, never()).logInvalidPhoneFormat(any())
    }

    @Test
    fun `should normalize and accept Costa Rican phone without dash`() {
        val phone = "88889999"
        val expectedNormalized = "+50688889999"

        authService.sendOtp(phone)

        verify(valueOperations).set(
            eq("otp:$expectedNormalized"),
            any<String>(),
            eq(otpProperties.expirationMinutes),
            eq(TimeUnit.MINUTES)
        )
        verify(securityAuditLogger).logOtpGenerated(expectedNormalized)
        verify(securityAuditLogger, never()).logInvalidPhoneFormat(any())
    }

    @Test
    fun `should reject invalid phone number format`() {
        val invalidPhone = "invalid-phone"

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.sendOtp(invalidPhone)
        }

        assertTrue(exception.message!!.contains("Invalid phone number format"))
        verify(valueOperations, never()).set(any(), any(), any<Long>(), any())
        verify(securityAuditLogger).logInvalidPhoneFormat(invalidPhone)
        verify(securityAuditLogger, never()).logOtpGenerated(any())
    }

    @Test
    fun `should reject empty phone number`() {
        val emptyPhone = ""

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.sendOtp(emptyPhone)
        }

        assertEquals("Phone number cannot be empty", exception.message)
        verify(valueOperations, never()).set(any(), any(), any<Long>(), any())
        verify(securityAuditLogger).logInvalidPhoneFormat(emptyPhone)
    }

    @Test
    fun `should handle phone validation in verifyOtpAndIssueTokens`() {
        val phone = "8888-9999"
        val expectedNormalized = "+50688889999"
        val code = "123456"

        whenever(valueOperations.get("otp_attempts:$expectedNormalized")).thenReturn(null)
        whenever(valueOperations.get("otp:$expectedNormalized")).thenReturn(code)
        whenever(redisTemplate.hasKey("otp_used:$expectedNormalized")).thenReturn(false)

        val mockUser = mock<Any> {
            on { toString() } doReturn "User(id=123)"
        }
        whenever(mockUser.javaClass.getDeclaredField("id")).thenReturn(mock())
        whenever(userService.findOrCreateUser(expectedNormalized)).thenReturn(mockUser)
        whenever(jwtService.generateAccessToken(any())).thenReturn("access-token")
        whenever(jwtService.generateRefreshToken(any())).thenReturn("refresh-token")

        // This should not throw an exception
        assertDoesNotThrow {
            authService.verifyOtpAndIssueTokens(phone, code)
        }

        verify(securityAuditLogger, never()).logInvalidPhoneFormat(any())
    }

    @Test
    fun `should reject invalid phone in verifyOtpAndIssueTokens`() {
        val invalidPhone = "invalid"
        val code = "123456"

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(invalidPhone, code)
        }

        assertTrue(exception.message!!.contains("Invalid phone number format"))
        verify(securityAuditLogger).logInvalidPhoneFormat(invalidPhone)
        verify(valueOperations, never()).get(any())
    }

    @Test
    fun `should clear existing OTP when sending new one`() {
        val phone = "+50688888888"

        authService.sendOtp(phone)

        verify(redisTemplate).delete("otp:$phone")
        verify(redisTemplate).delete("otp_used:$phone")
        verify(valueOperations).set(
            eq("otp:$phone"),
            any<String>(),
            eq(otpProperties.expirationMinutes),
            eq(TimeUnit.MINUTES)
        )
    }

    @Test
    fun `should handle whitespace in phone numbers`() {
        val phoneWithSpaces = "  8888-9999  "
        val expectedNormalized = "+50688889999"

        authService.sendOtp(phoneWithSpaces)

        verify(valueOperations).set(
            eq("otp:$expectedNormalized"),
            any<String>(),
            eq(otpProperties.expirationMinutes),
            eq(TimeUnit.MINUTES)
        )
        verify(securityAuditLogger).logOtpGenerated(expectedNormalized)
    }
}