package com.gasolinerajsm.coupon.service

import com.gasolinerajsm.coupon.config.CouponProperties
import com.gasolinerajsm.coupon.domain.CouponStatus
import com.gasolinerajsm.coupon.domain.QRCoupon
import com.gasolinerajsm.coupon.dto.GenerateQRRequest
import com.gasolinerajsm.coupon.dto.ScanQRRequest
import com.gasolinerajsm.coupon.dto.ActivateCouponRequest
import com.gasolinerajsm.coupon.repository.QRCouponRepository
// import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

@Service
@Transactional
class QRCouponService(
    private val couponRepository: QRCouponRepository,
    private val qrCodeGenerator: QRCodeGenerator,
    private val tokenGenerator: TokenGenerator,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val couponProperties: CouponProperties
) {

    fun generateQRCoupon(request: GenerateQRRequest): QRCoupon {
        // Validación adicional de negocio
        require(request.amount > 0) { "El monto debe ser positivo" }
        require(request.amount <= couponProperties.maxTicketsPerCoupon) {
            "El monto no puede exceder ${couponProperties.maxTicketsPerCoupon} tickets"
        }

        val token = tokenGenerator.generateUniqueToken()
        val qrCode = qrCodeGenerator.generateQRCode(token)
        val baseTickets = request.amount // 1 ticket por cada múltiplo de 5000

        val coupon = QRCoupon(
            qrCode = qrCode,
            token = token,
            stationId = request.stationId,
            employeeId = request.employeeId,
            amount = request.amount,
            baseTickets = baseTickets,
            totalTickets = baseTickets,
            expiresAt = LocalDateTime.now().plusHours(couponProperties.expirationHours)
        )

        val savedCoupon = couponRepository.save(coupon)

        // Cache QR code for quick lookup
        redisTemplate.opsForValue().set(
            "qr:${qrCode}",
            savedCoupon.id.toString(),
            couponProperties.expirationHours,
            TimeUnit.HOURS
        )

        return savedCoupon
    }

    fun scanQRCoupon(request: ScanQRRequest): QRCoupon {
        // Usar lock pessimistic para prevenir race conditions
        val coupon = couponRepository.findAndLockByQrCode(request.qrCode)
            ?: throw IllegalArgumentException("QR Code no válido")

        if (coupon.status != CouponStatus.GENERATED) {
            throw IllegalStateException("Este QR ya fue utilizado")
        }

        if (coupon.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalStateException("Este QR ha expirado")
        }

        val updatedCoupon = coupon.copy(
            status = CouponStatus.SCANNED,
            scannedBy = request.userId,
            scannedAt = LocalDateTime.now()
        )

        return couponRepository.save(updatedCoupon)
    }

    fun activateCoupon(request: ActivateCouponRequest): QRCoupon {
        val coupon = couponRepository.findById(request.couponId)
            .orElseThrow { IllegalArgumentException("Cupón no encontrado") }

        if (coupon.scannedBy != request.userId) {
            throw IllegalArgumentException("No tienes permisos para activar este cupón")
        }

        if (coupon.status != CouponStatus.SCANNED) {
            throw IllegalStateException("Este cupón no puede ser activado")
        }

        val updatedCoupon = coupon.copy(
            status = CouponStatus.ACTIVATED,
            activatedAt = LocalDateTime.now()
        )

        val savedCoupon = couponRepository.save(updatedCoupon)

        // TODO: Publicar evento para iniciar secuencia de anuncios (RabbitMQ disabled for now)
        // rabbitTemplate.convertAndSend(
        //     "coupon.exchange",
        //     "coupon.activated",
        //     mapOf(
        //         "couponId" to savedCoupon.id,
        //         "userId" to request.userId,
        //         "baseTickets" to savedCoupon.baseTickets
        //     )
        // )
        println("Would publish coupon.activated event for coupon ${savedCoupon.id}")

        return savedCoupon
    }

    fun getUserCoupons(userId: UUID, pageable: Pageable): Page<QRCoupon> {
        return couponRepository.findByScannedBy(userId, pageable)
    }

    fun getUserActiveTickets(userId: UUID): Int {
        val activeCoupons = couponRepository.findByUserIdAndStatuses(
            userId,
            listOf(CouponStatus.ACTIVATED, CouponStatus.COMPLETED)
        )
        return activeCoupons.sumOf { it.totalTickets }
    }

    fun getStationStats(stationId: UUID, days: Int = 30): Map<String, Any> {
        val stats = couponRepository.getStationStatsOptimized(stationId)

        return mapOf(
            "totalCoupons" to (stats[0] as Long).toInt(),
            "totalTickets" to (stats[1] as Long).toInt(),
            "activeCoupons" to (stats[2] as Long).toInt(),
            "expiredCoupons" to (stats[3] as Long).toInt()
        )
    }

    fun getEmployeeStats(employeeId: UUID, days: Int = 30): Map<String, Any> {
        val stats = couponRepository.getEmployeeStatsOptimized(employeeId)

        return mapOf(
            "totalCoupons" to (stats[0] as Long).toInt(),
            "totalTickets" to (stats[1] as Long).toInt(),
            "scannedCoupons" to (stats[2] as Long).toInt(),
            "conversionRate" to (stats[3] as Double)
        )
    }
}