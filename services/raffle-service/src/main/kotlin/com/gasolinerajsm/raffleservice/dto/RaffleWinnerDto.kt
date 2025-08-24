package com.gasolinerajsm.raffleservice.dto

import java.time.LocalDateTime

/**
 * Data Transfer Object for RaffleWinner information
 */
data class RaffleWinnerDto(
    val id: String,
    val raffleId: String,
    val participantId: String,
    val selectionDate: LocalDateTime,
    val merkleProof: String,
    val externalSeed: String,
    val selectionIndex: Int,
    val verified: Boolean,
    val prizeClaimed: Boolean,
    val claimDate: LocalDateTime?,
    val createdAt: LocalDateTime
)