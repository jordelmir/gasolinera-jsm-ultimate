package com.gasolinerajsm.authservice.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

class OtpPropertiesTest {

    @Test
    fun `should use default values when no configuration provided`() {
        val properties = OtpProperties()

        assertEquals(5L, properties.expirationMinutes)
        assertEquals(6, properties.length)
        assertEquals(5, properties.maxAttempts)
        assertEquals(15L, properties.lockoutMinutes)
        assertEquals(100000, properties.minValue)
        assertEquals(999999, properties.maxValue)
        assertTrue(properties.enableAuditLogging)
        assertFalse(properties.logSensitiveData)
    }

    @Test
    fun `should bind configuration properties correctly`() {
        val configMap = mapOf(
            "app.otp.expiration-minutes" to "10",
            "app.otp.length" to "4",
            "app.otp.max-attempts" to "3",
            "app.otp.lockout-minutes" to "30",
            "app.otp.min-value" to "1000",
            "app.otp.max-value" to "9999",
            "app.otp.enable-audit-logging" to "false",
            "app.otp.log-sensitive-data" to "true"
        )

        val source = MapConfigurationPropertySource(configMap)
        val binder = Binder(source)
        val properties = binder.bind("app.otp", OtpProperties::class.java).get()

        assertEquals(10L, properties.expirationMinutes)
        assertEquals(4, properties.length)
        assertEquals(3, properties.maxAttempts)
        assertEquals(30L, properties.lockoutMinutes)
        assertEquals(1000, properties.minValue)
        assertEquals(9999, properties.maxValue)
        assertFalse(properties.enableAuditLogging)
        assertTrue(properties.logSensitiveData)
    }

    @Test
    fun `should handle partial configuration with defaults`() {
        val configMap = mapOf(
            "app.otp.expiration-minutes" to "8",
            "app.otp.enable-audit-logging" to "false"
        )

        val source = MapConfigurationPropertySource(configMap)
        val binder = Binder(source)
        val properties = binder.bind("app.otp", OtpProperties::class.java).get()

        // Configured values
        assertEquals(8L, properties.expirationMinutes)
        assertFalse(properties.enableAuditLogging)

        // Default values
        assertEquals(6, properties.length)
        assertEquals(5, properties.maxAttempts)
        assertEquals(15L, properties.lockoutMinutes)
        assertEquals(100000, properties.minValue)
        assertEquals(999999, properties.maxValue)
        assertFalse(properties.logSensitiveData)
    }

    @Test
    fun `should validate secure default values`() {
        val properties = OtpProperties()

        // Security-focused assertions
        assertTrue(properties.expirationMinutes <= 10, "OTP expiration should be reasonably short")
        assertTrue(properties.maxAttempts <= 10, "Max attempts should prevent brute force")
        assertTrue(properties.lockoutMinutes >= 5, "Lockout should be long enough to deter attacks")
        assertTrue(properties.length >= 4, "OTP should be long enough to be secure")
        assertTrue(properties.enableAuditLogging, "Audit logging should be enabled by default")
        assertFalse(properties.logSensitiveData, "Sensitive data logging should be disabled by default")
    }

    @Test
    fun `should handle boolean string values correctly`() {
        val configMap = mapOf(
            "app.otp.enable-audit-logging" to "true",
            "app.otp.log-sensitive-data" to "false"
        )

        val source = MapConfigurationPropertySource(configMap)
        val binder = Binder(source)
        val properties = binder.bind("app.otp", OtpProperties::class.java).get()

        assertTrue(properties.enableAuditLogging)
        assertFalse(properties.logSensitiveData)
    }

    @Test
    fun `should maintain backward compatibility`() {
        // Test that existing configuration without new properties still works
        val configMap = mapOf(
            "app.otp.expiration-minutes" to "5",
            "app.otp.length" to "6",
            "app.otp.max-attempts" to "5",
            "app.otp.lockout-minutes" to "15",
            "app.otp.min-value" to "100000",
            "app.otp.max-value" to "999999"
        )

        val source = MapConfigurationPropertySource(configMap)
        val binder = Binder(source)
        val properties = binder.bind("app.otp", OtpProperties::class.java).get()

        // Original properties should work
        assertEquals(5L, properties.expirationMinutes)
        assertEquals(6, properties.length)
        assertEquals(5, properties.maxAttempts)
        assertEquals(15L, properties.lockoutMinutes)
        assertEquals(100000, properties.minValue)
        assertEquals(999999, properties.maxValue)

        // New properties should use defaults
        assertTrue(properties.enableAuditLogging)
        assertFalse(properties.logSensitiveData)
    }
}