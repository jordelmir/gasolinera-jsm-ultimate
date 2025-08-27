package com.gasolinerajsm.authservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.otp")
data class OtpProperties(
    var expirationMinutes: Long = 5,
    var length: Int = 6,
    var maxAttempts: Int = 5,
    var lockoutMinutes: Long = 15,
    var minValue: Int = 100000,
    var maxValue: Int = 999999,
    var enableAuditLogging: Boolean = true,
    var logSensitiveData: Boolean = false // Should be false in production
)