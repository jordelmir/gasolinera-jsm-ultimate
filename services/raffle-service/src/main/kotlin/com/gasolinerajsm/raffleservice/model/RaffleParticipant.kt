package com.gasolinerajsm.raffleservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Raffle participant entity representing a user's participation in a raffle
 */
@Entity
@Table(
    name = "raffle_participants",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["raffle_id", "user_id"])
    ]
)
data class RaffleParticipant(
    @Id
    val id: String = "",

    @Column(name = "raffle_id", nullable = false)
    val raffleId: String = "",

    @Column(name = "user_id", nullable = false)
    val userId: String = "",

    @Column(name = "participation_date", nullable = false)
    val participationDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "eligibility_proof", columnDefinition = "TEXT", nullable = false)
    val eligibilityProof: String = "",

    @Column(name = "entry_hash", length = 64, nullable = false)
    val entryHash: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Generate entry string for Merkle Tree
     */
    fun generateEntryString(): String {
        return "$userId:$raffleId:$participationDate:$eligibilityProof"
    }

    /**
     * Check if participation is valid
     */
    fun isValid(): Boolean {
        return userId.isNotBlank() &&
               raffleId.isNotBlank() &&
               eligibilityProof.isNotBlank() &&
               entryHash.isNotBlank()
    }
}