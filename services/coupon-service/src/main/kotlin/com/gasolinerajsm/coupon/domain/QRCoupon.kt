package com.gasolinerajsm.coupon.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "qr_coupons")
@EntityListeners(AuditingEntityListener::class)
data class QRCoupon(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val qrCode: String,

    @Column(unique = true, nullable = false)
    val token: String,

    @Column(nullable = false)
    val stationId: UUID,

    @Column(nullable = false)
    val employeeId: UUID,

    @Column(nullable = false)
    val amount: Int, // Múltiplos de 5000

    @Column(nullable = false)
    val baseTickets: Int, // Tickets base sin anuncios

    @Column(nullable = false)
    val bonusTickets: Int = 0, // Tickets bonus por anuncios

    @Column(nullable = false)
    val totalTickets: Int, // Total de tickets

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: CouponStatus = CouponStatus.GENERATED,

    @Column
    val scannedBy: UUID? = null,

    @Column
    val scannedAt: LocalDateTime? = null,

    @Column
    val activatedAt: LocalDateTime? = null,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class CouponStatus {
    GENERATED,    // QR generado por empleado
    SCANNED,      // Escaneado por cliente
    ACTIVATED,    // Activado por cliente (vio primer anuncio)
    COMPLETED,    // Proceso completado
    EXPIRED,      // Expirado
    USED_IN_RAFFLE // Usado en sorteo
}