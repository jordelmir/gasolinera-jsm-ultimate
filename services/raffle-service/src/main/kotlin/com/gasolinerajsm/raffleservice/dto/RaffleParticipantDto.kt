package com.gasolinerajsm.raffleservice.dto

import java.time.LocalDateTime

/**
 * Data Transfer Object for RaffleParticipant information
 */
data class RaffleParticipantDto(
    val id: String,
    val raffleId: String,
    val userId: String,
    val participationDate: LocalDateTime,
    val eligibilityProof: String,
    val entryHash: String,
    val createdAt: LocalDateTime
)