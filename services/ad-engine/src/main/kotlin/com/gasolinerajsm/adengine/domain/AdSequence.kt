package com.gasolinerajsm.adengine.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "ad_sequences")
@EntityListeners(AuditingEntityListener::class)
data class AdSequence(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val couponId: UUID,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val currentStep: Int = 1, // Paso actual en la secuencia

    @Column(nullable = false)
    val maxSteps: Int = 10, // Máximo 10 anuncios

    @Column(nullable = false)
    val baseTickets: Int,

    @Column(nullable = false)
    val currentTickets: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: SequenceStatus = SequenceStatus.ACTIVE,

    @Column
    val completedAtcalDateTime? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalTime.now()
)

enum class SequenceStatus {
    ACTIVE,     // Secuencia activa
    COMPLETED,  // Usuario completó todos los anuncios
    ABANDONED,  // Usuario abandonó la secuencia
    EXPIRED     // Secuencia expiró
}

// Configuración de duración de anuncios por paso
object AdDurationConfig {
    val DURATIONS = mapOf(
        1 to 10,   // 10 segundos
        2 to 15,   // 15 segundos
        3 to 30,   // 30 segundos
        4 to 60,   // 1 minuto
        5 to 120,  // 2 minutos
        6 to 180,  // 3 minutos
        7 to 240,  // 4 minutos
        8 to 300,  // 5 minutos
        9 to 420,  // 7 minutos
        10 to 600  // 10 minutos
    )

    fun getDuration(step: Int): Int = DURATIONS[step] ?: 600

    fun getTicketMultiplier(step: Int): Int = step // Cada paso duplica los tickets base
}