package com.gasolinerajsm.redemptionservice.domain.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "points_ledger")
data class PointsLedgerEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "points_credited", nullable = false)
    val pointsCredited: Int,

    @Column(name = "redemption_id", nullable = false)
    val redemptionId: UUID,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
