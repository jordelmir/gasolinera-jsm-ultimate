package com.gasolinerajsm.raffleservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "raffle_winners")
data class RaffleWinner(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val raffleId: Long,
    val userId: String,
    val winningPointId: String,
    val prize: String, // e.g., "10000 Puntos G"
    val awardedAt: LocalDateTime = LocalDateTime.now()
)
