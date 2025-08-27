package com.gasolinerajsm.coupon.controller

import com.gasolinerajsm.coupon.dto.*
import com.gasolinerajsm.coupon.service.QRCouponService
import com.gasolinerajsm.coupon.service.QRCodeGenerator
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/coupons")
class QRCouponController(
    private val couponService: QRCouponService,
    private val qrCodeGenerator: QRCodeGenerator
) {

    @PostMapping("/generate")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('OWNER')")
    fun generateQRCoupon(@Valid @RequestBody request: GenerateQRRequest): ResponseEntity<GenerateQRResponse> {
        val coupon = couponService.generateQRCoupon(request)
        val qrImage = qrCodeGenerator.generateQRImage(coupon.qrCode)
        val qrImageBase64 = Base64.getEncoder().encodeToString(qrImage)

        val response = GenerateQRResponse(
            couponId = coupon.id,
            qrCode = coupon.qrCode,
            qrImage = qrImageBase64,
            token = coupon.token,
            baseTickets = coupon.baseTickets,
            expiresAt = coupon.expiresAt
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/scan")
    @PreAuthorize("hasRole('CLIENT')")
    fun scanQRCoupon(@Valid @RequestBody request: ScanQRRequest): ResponseEntity<ScanQRResponse> {
        val coupon = couponService.scanQRCoupon(request)

        val response = ScanQRResponse(
            couponId = coupon.id,
            token = coupon.token,
            baseTickets = coupon.baseTickets,
            canActivate = true,
            message = "¡QR escaneado exitosamente! Toca 'Activar' para comenzar."
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/activate")
    @PreAuthorize("hasRole('CLIENT')")
    fun activateCoupon(@Valid @RequestBody request: ActivateCouponRequest): ResponseEntity<ActivateCouponResponse> {
        val coupon = couponService.activateCoupon(request)

        val response = ActivateCouponResponse(
            couponId = coupon.id,
            token = coupon.token,
            baseTickets = coupon.baseTickets,
            nextAdDuration = 10, // Primer anuncio de 10 segundos
            message = "¡Cupón activado! Mira el anuncio para duplicar tus tickets."
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('CLIENT') and #userId == authentication.principal.id")
    fun getUserCoupons(
        @PathVariable userId: UUID,
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<UserTicketsResponse> {
        val couponsPage = couponService.getUserCoupons(userId, pageable)
        val totalTickets = couponService.getUserActiveTickets(userId)

        val couponDetails = couponsPage.content.map { coupon ->
            CouponDetailsResponse(
                id = coupon.id,
                token = coupon.token,
                amount = coupon.amount,
                baseTickets = coupon.baseTickets,
                bonusTickets = coupon.bonusTickets,
                totalTickets = coupon.totalTickets,
                status = coupon.status,
                scannedAt = coupon.scannedAt,
                activatedAt = coupon.activatedAt,
                expiresAt = coupon.expiresAt,
                createdAt = coupon.createdAt
            )
        }

        val response = UserTicketsResponse(
            totalActiveTickets = totalTickets,
            coupons = couponDetails,
            weeklyRaffleEligible = totalTickets > 0,
            annualRaffleEligible = totalTickets > 0
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/station/{stationId}/stats")
    @PreAuthorize("hasRole('OWNER')")
    fun getStationStats(
        @PathVariable stationId: UUID,
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<StationStatsResponse> {
        val stats = couponService.getStationStats(stationId, days)

        val response = StationStatsResponse(
            stationId = stationId,
            totalCoupons = stats["totalCoupons"] as Int,
            totalTickets = stats["totalTickets"] as Int,
            activeCoupons = stats["activeCoupons"] as Int,
            expiredCoupons = stats["expiredCoupons"] as Int,
            conversionRate = if (stats["totalCoupons"] as Int > 0) {
                (stats["activeCoupons"] as Int).toDouble() / (stats["totalCoupons"] as Int) * 100
            } else 0.0,
            period = "${days} días"
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/employee/{employeeId}/stats")
    @PreAuthorize("hasRole('EMPLOYEE') and #employeeId == authentication.principal.id or hasRole('OWNER')")
    fun getEmployeeStats(
        @PathVariable employeeId: UUID,
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<EmployeeStatsResponse> {
        val stats = couponService.getEmployeeStats(employeeId, days)

        val response = EmployeeStatsResponse(
            employeeId = employeeId,
            totalCoupons = stats["totalCoupons"] as Int,
            totalTickets = stats["totalTickets"] as Int,
            scannedCoupons = stats["scannedCoupons"] as Int,
            conversionRate = stats["conversionRate"] as Double,
            period = "${days} días"
        )

        return ResponseEntity.ok(response)
    }
}