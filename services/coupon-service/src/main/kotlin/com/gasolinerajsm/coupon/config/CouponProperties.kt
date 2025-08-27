package com.gasolinerajsm.coupon.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.coupon")
data class CouponProperties(
    var expirationHours: Long = 24,
    var maxTicketsPerCoupon: Int = 10,
    var qrSignatureSecret: String = "default-secret-change-in-production"
)