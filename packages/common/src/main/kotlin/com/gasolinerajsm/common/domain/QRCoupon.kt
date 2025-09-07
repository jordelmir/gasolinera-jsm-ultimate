package com.gasolinerajsm.common.domain

import java.time.LocalDateTime
import java.util.UUID

data class QRCoupon(
    val id: UUID,
    val couponCode: String,
    val userId: String?,
    val stationId: String,
    val status: CouponStatus,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val redeemedAt: LocalDateTime? = null
)
