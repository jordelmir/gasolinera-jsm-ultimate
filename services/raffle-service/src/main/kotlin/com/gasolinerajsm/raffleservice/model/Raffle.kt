package com.gasolinerajsm.raffleservice.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "raffles")
data class Raffle(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val period: String, // e.g., "2025-08"
    val merkleRoot: String,
    @Enumerated(EnumType.STRING)
    var status: RaffleStatus = RaffleStatus.OPEN,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var drawAt: LocalDateTime? = null,
    var externalSeed: String? = null,
    var winnerEntryId: String? = null // ID del punto ganador
)

enum class RaffleStatus {
    OPEN, CLOSED, DRAWN
}
