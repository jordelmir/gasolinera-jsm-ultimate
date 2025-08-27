package com.gasolinerajsm.authservice.service

import com.gasolinerajsm.authservice.config.OtpProperties
import com.gasolinerajsm.authservice.security.SecurityAuditLogger
import com.gasolinerajsm.authservice.validation.PhoneNumberValidator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations

class AuthServiceSecureLoggingTest {

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var jwtService: JwtService
    private lateinit var userService: UserService
    private lateinit var otpProperties: OtpProperties
    private lateinit var phoneValidator: PhoneNumberValidator
    private lateinit var securityAuditLogger: SecurityAuditLogger
    private lateinit var authService: AuthService
    private lateinit var valueOperations: ValueOperations<String, String>
    private lateinit var listAppender: ListAppender<ILoggingEvent>
    private lateinit var logger: Logger

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
            maxAttempts = 5
            lockoutMinutes = 15
            enableAuditLogging = true
            logSensitiveData = false
        }

        phoneValidator = PhoneNumberValidator()
        securityAuditLogger = SecurityAuditLogger(otpProperties)

        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(redisTemplate.hasKey(any())).thenReturn(false)

        authService = AuthService(redisTemplate, jwtService, userService, otpProperties, phoneValidator, securityAuditLogger)

        // Set up log capture for AuthService logger
        logger = LoggerFactory.getLogger(AuthService::class.java) as Logger
        listAppender = ListAppender()
        listAppender.start()
        logger.addAppender(listAppender)
    }

    @Test
    fun `should not log phone numbers in AuthService logs`() {
        val phone = "+50688888888"

        authService.sendOtp(phone)

        val logEvents = listAppender.list

        // Check all log messages don't contain the phone number
        logEvents.forEach { logEvent ->
            assertFalse(
                logEvent.message.contains("88888888") || logEvent.formattedMessage.contains("88888888"),
                "Log message should not contain phone number: ${logEvent.formattedMessage}"
            )
            assertFalse(
                logEvent.message.contains(phone) || logEvent.formattedMessage.contains(phone),
                "Log message should not contain full phone number: ${logEvent.formattedMessage}"
            )
        }

        // Should have logged something (but without sensitive data)
        assertTrue(logEvents.isNotEmpty(), "Should have logged OTP generation")

        // Verify the log message is generic
        val otpLogEvent = logEvents.find { it.formattedMessage.contains("Generated OTP") }
        assertNotNull(otpLogEvent, "Should have logged OTP generation")
        assertEquals("Generated OTP for phone number", otpLogEvent!!.formattedMessage)
    }

    @Test
    fun `should not log OTP codes in any logs`() {
        val phone = "+50688888888"
        val otp = "123456"

        // Mock to return a specific OTP for testing
        whenever(valueOperations.get("otp:$phone")).thenReturn(otp)

        authService.sendOtp(phone)

        val logEvents = listAppender.list

        // Check that no log message contains the OTP code
        logEvents.forEach { logEvent ->
            assertFalse(
                logEvent.message.contains(otp) || logEvent.formattedMessage.contains(otp),
                "Log message should not contain OTP code: ${logEvent.formattedMessage}"
            )
        }

        // Verify no debug logs contain OTP
        val debugEvents = logEvents.filter { it.level.toString() == "DEBUG" }
        debugEvents.forEach { logEvent ->
            assertFalse(
                logEvent.message.contains("Mock OTP") || logEvent.formattedMessage.contains("Mock OTP"),
                "Debug log should not contain OTP reference: ${logEvent.formattedMessage}"
            )
        }
    }

    @Test
    fun `should use generic logging for successful verification`() {
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

        val logEvents = listAppender.list

        // Should not contain phone number in any log
        logEvents.forEach { logEvent ->
            assertFalse(
                logEvent.message.contains("88888888") || logEvent.formattedMessage.contains("88888888"),
                "Log message should not contain phone number: ${logEvent.formattedMessage}"
            )
            assertFalse(
                logEvent.message.contains(phone) || logEvent.formattedMessage.contains(phone),
                "Log message should not contain full phone number: ${logEvent.formattedMessage}"
            )
        }

        // Should not contain OTP code
        logEvents.forEach { logEvent ->
            assertFalse(
                logEvent.message.contains(correctCode) || logEvent.formattedMessage.contains(correctCode),
                "Log message should not contain OTP code: ${logEvent.formattedMessage}"
            )
        }
    }

    @Test
    fun `should use generic logging for token revocation`() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

        authService.revokeToken(token)

        val logEvents = listAppender.list

        // Should not contain the actual token in logs
        logEvents.forEach { logEvent ->
            assertFalse(
                logEvent.message.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9") ||
                logEvent.formattedMessage.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"),
                "Log message should not contain token content: ${logEvent.formattedMessage}"
            )
        }

        // Should have logged the revocation generically
        val revocationLog = logEvents.find { it.formattedMessage.contains("Token revoked successfully") }
        assertNotNull(revocationLog, "Should have logged token revocation")
    }

    @Test
    fun `should not expose sensitive data in error scenarios`() {
        val phone = "+50688888888"
        val wrongCode = "000000"
        val correctCode = "123456"

        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn("1")
        whenever(valueOperations.get("otp:$phone")).thenReturn(correctCode)
        whenever(redisTemplate.hasKey("otp_used:$phone")).thenReturn(false)

        assertThrows(IllegalArgumentException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, wrongCode)
        }

        val logEvents = listAppender.list

        // Should not contain phone number or OTP codes in error logs
        logEvents.forEach { logEvent ->
            assertFalse(
                logEvent.message.contains("88888888") || logEvent.formattedMessage.contains("88888888"),
                "Error log should not contain phone number: ${logEvent.formattedMessage}"
            )
            assertFalse(
                logEvent.message.contains(wrongCode) || logEvent.formattedMessage.contains(wrongCode),
                "Error log should not contain wrong OTP: ${logEvent.formattedMessage}"
            )
            assertFalse(
                logEvent.message.contains(correctCode) || logEvent.formattedMessage.contains(correctCode),
                "Error log should not contain correct OTP: ${logEvent.formattedMessage}"
            )
        }
    }

    @Test
    fun `should handle lockout scenarios without exposing sensitive data`() {
        val phone = "+50688888888"
        val code = "123456"

        // At max attempts
        whenever(valueOperations.get("otp_attempts:$phone")).thenReturn("5")

        assertThrows(IllegalStateException::class.java) {
            authService.verifyOtpAndIssueTokens(phone, code)
        }

        val logEvents = listAppender.list

        // Should not contain phone number in lockout logs
        logEvents.forEach { logEvent ->
            assertFalse(
                logEvent.message.contains("88888888") || logEvent.formattedMessage.contains("88888888"),
                "Lockout log should not contain phone number: ${logEvent.formattedMessage}"
            )
            assertFalse(
                logEvent.message.contains(phone) || logEvent.formattedMessage.contains(phone),
                "Lockout log should not contain full phone number: ${logEvent.formattedMessage}"
            )
        }
    }

    @Test
    fun `should verify SecurityAuditLogger is used for sensitive operations`() {
        val phone = "+50688888888"
        val securityAuditLoggerSpy = spy(securityAuditLogger)

        val authServiceWithSpy = AuthService(redisTemplate, jwtService, userService, otpProperties, phoneValidator, securityAuditLoggerSpy)

        authServiceWithSpy.sendOtp(phone)

        // Verify SecurityAuditLogger is called instead of direct logging
        verify(securityAuditLoggerSpy).logOtpGenerated(phone)

        // AuthService logs should be generic
        val logEvents = listAppender.list
        val otpLog = logEvents.find { it.formattedMessage.contains("Generated OTP") }
        assertNotNull(otpLog)
        assertEquals("Generated OTP for phone number", otpLog!!.formattedMessage)
    }

    @Test
    fun `should not log sensitive data even when debug logging is enabled`() {
        // Enable debug logging
        logger.level = ch.qos.logback.classic.Level.DEBUG

        val phone = "+50688888888"

        authService.sendOtp(phone)

        val logEvents = listAppender.list
        val debugEvents = logEvents.filter { it.level.toString() == "DEBUG" }

        // Even debug logs should not contain sensitive data
        debugEvents.forEach { logEvent ->
            assertFalse(
                logEvent.message.contains("88888888") || logEvent.formattedMessage.contains("88888888"),
                "Debug log should not contain phone number: ${logEvent.formattedMessage}"
            )
            assertFalse(
                logEvent.message.contains("Mock OTP") || logEvent.formattedMessage.contains("Mock OTP"),
                "Debug log should not reference OTP: ${logEvent.formattedMessage}"
            )
        }
    }
}