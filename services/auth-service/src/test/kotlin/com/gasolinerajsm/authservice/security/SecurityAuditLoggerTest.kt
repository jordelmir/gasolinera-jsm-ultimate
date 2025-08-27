package com.gasolinerajsm.authservice.security

import com.gasolinerajsm.authservice.config.OtpProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory

class SecurityAuditLoggerTest {

    private lateinit var otpProperties: OtpProperties
    private lateinit var securityAuditLogger: SecurityAuditLogger
    private lateinit var listAppender: ListAppender<ILoggingEvent>
    private lateinit var logger: Logger

    @BeforeEach
    fun setUp() {
        otpProperties = OtpProperties().apply {
            enableAuditLogging = true
            logSensitiveData = false
        }

        securityAuditLogger = SecurityAuditLogger(otpProperties)

        // Set up log capture
        logger = LoggerFactory.getLogger("SECURITY_AUDIT") as Logger
        listAppender = ListAppender()
        listAppender.start()
        logger.addAppender(listAppender)
    }

    @Test
    fun `should log OTP generation with hashed phone number`() {
        val phoneNumber = "+50688888888"

        securityAuditLogger.logOtpGenerated(phoneNumber)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        assertEquals("INFO", logEvent.level.toString())
        assertTrue(logEvent.message.contains("OTP generated for phone hash"))
        assertFalse(logEvent.message.contains(phoneNumber))

        // Verify MDC contains expected keys
        val mdc = logEvent.mdcPropertyMap
        assertEquals("OTP_GENERATED", mdc["event_type"])
        assertNotNull(mdc["phone_hash"])
        assertNotNull(mdc["timestamp"])
        assertFalse(mdc["phone_hash"]!!.contains("88888888"))
    }

    @Test
    fun `should log OTP verification failure with attempt count`() {
        val phoneNumber = "+50688888888"
        val attemptCount = 3

        securityAuditLogger.logOtpVerificationFailed(phoneNumber, attemptCount)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        assertEquals("WARN", logEvent.level.toString())
        assertTrue(logEvent.message.contains("OTP verification failed"))
        assertTrue(logEvent.message.contains("attempt 3"))

        val mdc = logEvent.mdcPropertyMap
        assertEquals("OTP_VERIFICATION_FAILED", mdc["event_type"])
        assertEquals("3", mdc["attempt_count"])
        assertNotNull(mdc["phone_hash"])
        assertNotNull(mdc["timestamp"])
    }

    @Test
    fun `should log account lockout`() {
        val phoneNumber = "+50688888888"

        securityAuditLogger.logAccountLocked(phoneNumber)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        assertEquals("WARN", logEvent.level.toString())
        assertTrue(logEvent.message.contains("Account locked"))

        val mdc = logEvent.mdcPropertyMap
        assertEquals("ACCOUNT_LOCKED", mdc["event_type"])
        assertNotNull(mdc["phone_hash"])
        assertNotNull(mdc["timestamp"])
    }

    @Test
    fun `should log token revocation with safe token identifier`() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

        securityAuditLogger.logTokenRevoked(token)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        assertEquals("WARN", logEvent.level.toString())
        assertTrue(logEvent.message.contains("JWT token revoked"))

        val mdc = logEvent.mdcPropertyMap
        assertEquals("TOKEN_REVOKED", mdc["event_type"])
        assertNotNull(mdc["token_id"])
        assertNotNull(mdc["timestamp"])

        // Verify token is not fully logged
        val tokenId = mdc["token_id"]!!
        assertTrue(tokenId.contains("..."))
        assertFalse(tokenId.contains("eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"))
    }

    @Test
    fun `should log successful authentication`() {
        val userId = "user123"
        val phoneNumber = "+50688888888"

        securityAuditLogger.logSuccessfulAuthentication(userId, phoneNumber)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        assertEquals("INFO", logEvent.level.toString())
        assertTrue(logEvent.message.contains("Successful authentication"))
        assertTrue(logEvent.message.contains(userId))

        val mdc = logEvent.mdcPropertyMap
        assertEquals("AUTHENTICATION_SUCCESS", mdc["event_type"])
        assertEquals(userId, mdc["user_id"])
        assertNotNull(mdc["phone_hash"])
        assertNotNull(mdc["timestamp"])
    }

    @Test
    fun `should not log when audit logging is disabled`() {
        otpProperties.enableAuditLogging = false
        securityAuditLogger = SecurityAuditLogger(otpProperties)

        securityAuditLogger.logOtpGenerated("+50688888888")
        securityAuditLogger.logOtpVerificationFailed("+50688888888", 1)
        securityAuditLogger.logAccountLocked("+50688888888")

        val logEvents = listAppender.list
        assertEquals(0, logEvents.size)
    }

    @Test
    fun `should show masked phone when sensitive data logging enabled`() {
        otpProperties.logSensitiveData = true
        securityAuditLogger = SecurityAuditLogger(otpProperties)

        val phoneNumber = "+50688888888"
        securityAuditLogger.logOtpGenerated(phoneNumber)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val mdc = logEvents[0].mdcPropertyMap
        val phoneHash = mdc["phone_hash"]!!
        assertTrue(phoneHash.startsWith("***"))
        assertTrue(phoneHash.endsWith("8888"))
    }

    @Test
    fun `should hash phone number when sensitive data logging disabled`() {
        otpProperties.logSensitiveData = false
        securityAuditLogger = SecurityAuditLogger(otpProperties)

        val phoneNumber = "+50688888888"
        securityAuditLogger.logOtpGenerated(phoneNumber)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val mdc = logEvents[0].mdcPropertyMap
        val phoneHash = mdc["phone_hash"]!!

        // Should be a hex hash (16 characters)
        assertEquals(16, phoneHash.length)
        assertTrue(phoneHash.matches(Regex("[0-9a-f]+")))
        assertFalse(phoneHash.contains("8888"))
    }

    @Test
    fun `should handle short tokens safely`() {
        val shortToken = "short"

        securityAuditLogger.logTokenRevoked(shortToken)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val mdc = logEvents[0].mdcPropertyMap
        assertEquals("***TOKEN***", mdc["token_id"])
    }

    @Test
    fun `should log OTP verification success`() {
        val phoneNumber = "+50688888888"
        val userId = "user123"

        securityAuditLogger.logOtpVerificationSuccess(phoneNumber, userId)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        assertEquals("INFO", logEvent.level.toString())
        assertTrue(logEvent.message.contains("OTP verification successful"))

        val mdc = logEvent.mdcPropertyMap
        assertEquals("OTP_VERIFICATION_SUCCESS", mdc["event_type"])
        assertEquals(userId, mdc["user_id"])
        assertNotNull(mdc["phone_hash"])
        assertNotNull(mdc["timestamp"])
    }

    @Test
    fun `should log invalid phone format attempts`() {
        val invalidPhone = "invalid-phone"

        securityAuditLogger.logInvalidPhoneFormat(invalidPhone)

        val logEvents = listAppender.list
        assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        assertEquals("WARN", logEvent.level.toString())
        assertTrue(logEvent.message.contains("Invalid phone number format"))

        val mdc = logEvent.mdcPropertyMap
        assertEquals("INVALID_PHONE_FORMAT", mdc["event_type"])
        assertNotNull(mdc["phone_hash"])
        assertNotNull(mdc["timestamp"])
    }
}