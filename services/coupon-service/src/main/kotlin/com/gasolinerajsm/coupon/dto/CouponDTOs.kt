package com.gasolinerajsm.coupon.dto

import com.gasolinerajsm.coupon.domain.CouponStatus
import java.time.LocalDateTime
import java.util.*

data class GenerateQRRequest(
    val stationId: UUID,
    val employeeId: UUID,
    val amount: Int // Múltiplos de 5000
)

data class GenerateQRResponse(
    val couponId: UUID,
    val qrCode: String,
    val qrImage: String, // Base64 encoded image
    val token: String,
    val baseTickets: Int,
    val expiresAt: LocalDateTime
)

data class ScanQRRequest(
    val qrCode: String,
    val userId: UUID
)

data class ScanQRResponse(
    val couponId: UUID,
    val token: String,
    val baseTickets: Int,
    val canActivate: Boolean,
    val message: String
)

data class ActivateCouponRequest(
    val couponId: UUID,
    val userId: UUID
)

data class ActivateCouponResponse(
    val couponId: UUID,
    val token: String,
    val baseTickets: Int,
    val nextAdDuration: Int, // Duración del primer anuncio en segundos
    val message: String
)

data class CouponDetailsResponse(
    val id: UUID,
    val token: String,
    val amount: Int,
    val baseTickets: Int,
    val bonusTickets: Int,
    val totalTickets: Int,
    val status: CouponStatus,
    val scannedAt: LocalDateTime?,
    val activatedAt: LocalDateTime?,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime
)

data class UserTicketsResponse(
    val totalActiveTickets: Int,
    val coupons: List<CouponDetailsResponse>,
    val weeklyRaffleEligible: Boolean,
    val annualRaffleEligible: Boolean
)

data class StationStatsResponse(
    val stationId: UUID,
    val totalCoupons: Int,
    val totalTickets: Int,
    val activeCoupons: Int,
    val expiredCoupons: Int,
    val conversionRate: Double,
    val period: String
)

data class EmployeeStatsResponse(
    val employeeId: UUID,
    val totalCoupons: Int,
    val totalTickets: Int,
    val scannedCoupons: Int,
    val conversionRate: Double,
    val period: String
)