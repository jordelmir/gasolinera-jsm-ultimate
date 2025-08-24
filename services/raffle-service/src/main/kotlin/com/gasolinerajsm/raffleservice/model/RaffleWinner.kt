package com.gasolinerajsm.raffleservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Raffle winner entity representing the selected winner of a raffle
 */
@Entity
@Table(name = "raffle_winners")
data class RaffleWinner(
    @Id
    val id: String = "",

    @Column(name = "raffle_id", nullable = false)
    val raffleId: String = "",

    @Column(name = "participant_id", nullable = false)
    val participantId: String = "",

    @Column(name = "selection_date", nullable = false)
    val selectionDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "merkle_proof", columnDefinition = "JSONB", nullable = false)
    val merkleProof: String = "[]", // JSON array of proof hashes

    @Column(name = "external_seed", nullable = false)
    val externalSeed: String = "",

    @Column(name = "selection_index", nullable = false)
    val selectionIndex: Int = 0,

    @Column(nullable = false)
    var verified: Boolean = false,

    @Column(name = "prize_claimed")
    var prizeClaimed: Boolean = false,

    @Column(name = "claim_date")
    var claimDate: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Mark prize as claimed
     */
    fun claimPrize() {
        this.prizeClaimed = true
        this.claimDate = LocalDateTime.now()
    }

    /**
     * Verify the winner selection
     */
    fun verify() {
        this.verified = true
    }

    /**
     * Check if winner can claim prize
     */
    fun canClaimPrize(): Boolean {
        return verified && !prizeClaimed
    }
}