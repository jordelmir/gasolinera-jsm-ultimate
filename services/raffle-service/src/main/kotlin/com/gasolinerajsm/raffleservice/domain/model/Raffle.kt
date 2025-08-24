package com.gasolinerajsm.raffleservice.domain.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "raffles")
data class Raffle(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val period: String,

    @Column(name = "merkle_root", nullable = false)
    val merkleRoot: String,

    @Column(nullable = false)
    val status: String, // e.g., 'CREATED', 'DRAWN'

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "drawn_at")
    var drawnAt: Instant? = null,

    @Column(name = "seed_source")
    var seedSource: String? = null,

    @Column(name = "seed_value")
    var seedValue: String? = null,

    @Column(name = "winner_point_id")
    var winnerPointId: String? = null
)
