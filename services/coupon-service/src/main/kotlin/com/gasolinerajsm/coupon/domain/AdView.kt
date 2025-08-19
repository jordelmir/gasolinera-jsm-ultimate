package com.gasolinerajsm.coupon.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "ad_views")
@EntityListeners(AuditingEntityListener::class)
data class AdView(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val couponId: UUID,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val adId: String,

    @Column(nullable = false)
    val duration: Int, // Duración en segundos

    @Column(nullable = false)
    val sequence: Int, // Secuencia del anuncio (1, 2, 3...)

    @Column(nullable = false)
    val ticketsEarned: Int, // Tickets ganados por este anuncio

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AdViewStatus = AdViewStatus.STARTED,

    @Column
    val completedAt: LocalDateTime? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class AdViewStatus {
    STARTED,    // Anuncio iniciado
    COMPLETED,  // Anuncio completado
    SKIPPED,    // Anuncio saltado
    FAILED      // Error en reproducción
}