package com.gasolinerajsm.raffleservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Raffle entity representing a lottery/raffle in the system
 */
@Entity
@Table(name = "raffles")
data class Raffle(
    @Id
    val id: String = "",

    @Column(nullable = false)
    val name: String = "",

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDateTime = LocalDateTime.now().plusDays(7),

    @Column(name = "max_participants", nullable = false)
    val maxParticipants: Int = 1000,

    @Column(name = "prize_description", columnDefinition = "TEXT", nullable = false)
    val prizeDescription: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RaffleStatus = RaffleStatus.OPEN,

    @Column(name = "merkle_root", length = 64)
    var merkleRoot: String? = null,

    @Column(name = "external_seed")
    var externalSeed: String? = null,

    @Column(name = "winner_entry_id")
    var winnerEntryId: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Check if raffle is active and accepting participants
     */
    fun isActive(): Boolean {
        val now = LocalDateTime.now()
        return status == RaffleStatus.OPEN &&
               now.isAfter(startDate) &&
               now.isBefore(endDate)
    }

    /**
     * Check if raffle can be drawn
     */
    fun canBeDrwan(): Boolean {
        val now = LocalDateTime.now()
        return status == RaffleStatus.OPEN && now.isAfter(endDate)
    }

    /**
     * Mark raffle as closed
     */
    fun close() {
        this.status = RaffleStatus.CLOSED
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Set winner and complete raffle
     */
    fun setWinner(entryId: String, merkleRoot: String, externalSeed: String) {
        this.winnerEntryId = entryId
        this.merkleRoot = merkleRoot
        this.externalSeed = externalSeed
        this.status = RaffleStatus.COMPLETED
        this.updatedAt = LocalDateTime.now()
    }
}

/**
 * Raffle status enumeration
 */
enum class RaffleStatus {
    OPEN,       // Accepting participants
    CLOSED,     // No longer accepting participants, ready for draw
    COMPLETED,  // Draw completed, winner selected
    CANCELLED   // Raffle cancelled
}