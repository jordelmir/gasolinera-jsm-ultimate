package com.gasolinerajsm.authservice.monitoring

import com.gasolinerajsm.authservice.config.OtpProperties
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import java.util.concurrent.TimeUnit

class SecurityMonitorTest {

    private lateinit var meterRegistry: MeterRegistry
    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var otpProperties: OtpProperties
    private lateinit var securityMonitor: SecurityMonitor
    private lateinit var zSetOperations: ZSetOperations<String, String>

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        redisTemplate = mock()
        zSetOperations = mock()

        otpProperties = OtpProperties().apply {
            maxAttempts = 5
            enableAuditLogging = true
        }

        whenever(redisTemplate.opsForZSet()).thenReturn(zSetOperations)
        whenever(redisTemplate.expire(any(), any<Long>(), any<TimeUnit>())).thenReturn(true)

        securityMonitor = SecurityMonitor(meterRegistry, redisTemplate, otpProperties)
    }

    @Test
    fun `should record OTP generation metrics`() {
        val phoneHash = "test-phone-hash"

        securityMonitor.recordOtpGenerated(phoneHash)

        val counter = meterRegistry.find("auth.otp.generated").counter()
        assertNotNull(counter)
        assertEquals(1.0, counter.count(), 0.01)
    }

    @Test
    fun `should record OTP verification success metrics`() {
        val phoneHash = "test-phone-hash"
        val userId = "user123"

        securityMonitor.recordOtpVerificationSuccess(phoneHash, userId)

        val counter = meterRegistry.find("auth.otp.verification.success").counter()
        assertNotNull(counter)
        assertEquals(1.0, counter.count(), 0.01)
    }

    @Test
    fun `should record OTP verification failure metrics`() {
        val phoneHash = "test-phone-hash"
        val attemptCount = 3

        securityMonitor.recordOtpVerificationFailure(phoneHash, attemptCount)

        val counter = meterRegistry.find("auth.otp.verification.failure").counter()
        assertNotNull(counter)
        assertEquals(1.0, counter.count(), 0.01)
    }

    @Test
    fun `should record account lockout metrics`() {
        val phoneHash = "test-phone-hash"

        securityMonitor.recordAccountLockout(phoneHash)

        val counter = meterRegistry.find("auth.account.lockout").counter()
        assertNotNull(counter)
        assertEquals(1.0, counter.count(), 0.01)
    }

    @Test
    fun `should record token revocation metrics`() {
        val tokenId = "token123"

        securityMonitor.recordTokenRevocation(tokenId)

        val counter = meterRegistry.find("auth.token.revocation").counter()
        assertNotNull(counter)
        assertEquals(1.0, counter.count(), 0.01)
    }

    @Test
    fun `should record invalid phone attempt metrics`() {
        val phoneHash = "invalid-phone-hash"

        securityMonitor.recordInvalidPhoneAttempt(phoneHash)

        val counter = meterRegistry.find("auth.phone.invalid").counter()
        assertNotNull(counter)
        assertEquals(1.0, counter.count(), 0.01)
    }

    @Test
    fun `should detect high frequency requests`() {
        val phoneHash = "frequent-requester"

        // Simulate high frequency requests (more than threshold)
        repeat(25) {
            securityMonitor.recordOtpGenerated(phoneHash)
        }

        // Should have recorded all requests
        val counter = meterRegistry.find("auth.otp.generated").counter()
        assertEquals(25.0, counter.count(), 0.01)

        // Should have detected suspicious activity (this would trigger alerts in real implementation)
        // We can't easily test the internal suspicious activity tracking without exposing it
        // but the metrics should be recorded
    }

    @Test
    fun `should track phone enumeration attempts`() {
        val phoneHash = "enumeration-attempt"

        whenever(zSetOperations.add(any(), any(), any<Double>())).thenReturn(true)
        whenever(zSetOperations.count(any(), any<Double>(), any<Double>())).thenReturn(25L)

        securityMonitor.recordInvalidPhoneAttempt(phoneHash)

        verify(zSetOperations).add(eq("suspicious:enumeration"), eq(phoneHash), any<Double>())
        verify(redisTemplate).expire(eq("suspicious:enumeration"), eq(10L), eq(TimeUnit.MINUTES))
    }

    @Test
    fun `should track token revocation patterns`() {
        val tokenId = "revoked-token"

        whenever(zSetOperations.add(any(), any(), any<Double>())).thenReturn(true)
        whenever(zSetOperations.count(any(), any<Double>(), any<Double>())).thenReturn(5L)

        securityMonitor.recordTokenRevocation(tokenId)

        verify(zSetOperations).add(eq("suspicious:revocations"), eq(tokenId), any<Double>())
        verify(redisTemplate).expire(eq("suspicious:revocations"), eq(10L), eq(TimeUnit.MINUTES))
    }

    @Test
    fun `should provide comprehensive security metrics`() {
        // Generate various events
        securityMonitor.recordOtpGenerated("phone1")
        securityMonitor.recordOtpGenerated("phone2")
        securityMonitor.recordOtpVerificationSuccess("phone1", "user1")
        securityMonitor.recordOtpVerificationFailure("phone2", 1)
        securityMonitor.recordAccountLockout("phone3")
        securityMonitor.recordTokenRevocation("token1")
        securityMonitor.recordInvalidPhoneAttempt("invalid1")

        val metrics = securityMonitor.getSecurityMetrics()

        assertEquals(2L, metrics.otpGenerated)
        assertEquals(1L, metrics.otpVerificationSuccess)
        assertEquals(1L, metrics.otpVerificationFailure)
        assertEquals(1L, metrics.accountLockouts)
        assertEquals(1L, metrics.tokenRevocations)
        assertEquals(1L, metrics.invalidPhoneAttempts)
        assertTrue(metrics.averageAuthTime >= 0.0)
    }

    @Test
    fun `should handle Redis failures gracefully`() {
        // Simulate Redis failure
        whenever(zSetOperations.add(any(), any(), any<Double>())).thenThrow(RuntimeException("Redis connection failed"))

        // Should not throw exception
        assertDoesNotThrow {
            securityMonitor.recordInvalidPhoneAttempt("test-phone")
        }

        // Metrics should still be recorded
        val counter = meterRegistry.find("auth.phone.invalid").counter()
        assertEquals(1.0, counter.count(), 0.01)
    }

    @Test
    fun `should clear suspicious activity on successful authentication`() {
        val phoneHash = "suspicious-phone"

        // First record some failures
        securityMonitor.recordOtpVerificationFailure(phoneHash, 1)
        securityMonitor.recordOtpVerificationFailure(phoneHash, 2)

        // Then record success
        securityMonitor.recordOtpVerificationSuccess(phoneHash, "user123")

        // Should have cleared suspicious activity (verified by Redis delete call)
        verify(redisTemplate).delete("suspicious:$phoneHash")
    }

    @Test
    fun `should track authentication timing`() {
        val phoneHash = "timing-test"
        val userId = "user123"

        securityMonitor.recordOtpVerificationSuccess(phoneHash, userId)

        val timer = meterRegistry.find("auth.authentication.duration").timer()
        assertNotNull(timer)
        assertTrue(timer.count() > 0)
    }

    @Test
    fun `should handle concurrent metric recording safely`() {
        val phoneHash = "concurrent-test"

        // Simulate concurrent access
        val threads = (1..10).map { threadNum ->
            Thread {
                repeat(10) {
                    securityMonitor.recordOtpGenerated("$phoneHash-$threadNum")
                    securityMonitor.recordOtpVerificationSuccess("$phoneHash-$threadNum", "user$threadNum")
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // All events should be recorded
        val otpCounter = meterRegistry.find("auth.otp.generated").counter()
        val successCounter = meterRegistry.find("auth.otp.verification.success").counter()

        assertEquals(100.0, otpCounter.count(), 0.01)
        assertEquals(100.0, successCounter.count(), 0.01)
    }

    @Test
    fun `should provide accurate metrics summary`() {
        // Create a known set of events
        repeat(5) { securityMonitor.recordOtpGenerated("phone$it") }
        repeat(3) { securityMonitor.recordOtpVerificationSuccess("phone$it", "user$it") }
        repeat(2) { securityMonitor.recordOtpVerificationFailure("phone$it", 1) }
        securityMonitor.recordAccountLockout("locked-phone")
        securityMonitor.recordTokenRevocation("revoked-token")
        repeat(3) { securityMonitor.recordInvalidPhoneAttempt("invalid$it") }

        val metrics = securityMonitor.getSecurityMetrics()

        assertEquals(5L, metrics.otpGenerated)
        assertEquals(3L, metrics.otpVerificationSuccess)
        assertEquals(2L, metrics.otpVerificationFailure)
        assertEquals(1L, metrics.accountLockouts)
        assertEquals(1L, metrics.tokenRevocations)
        assertEquals(3L, metrics.invalidPhoneAttempts)

        // Verify the metrics are consistent
        assertTrue(metrics.otpVerificationSuccess + metrics.otpVerificationFailure <= metrics.otpGenerated)
    }
}