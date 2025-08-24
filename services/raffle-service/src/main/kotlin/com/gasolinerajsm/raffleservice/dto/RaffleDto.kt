package com.gasolinerajsm.raffleservice.dto

import com.gasolinerajsm.raffleservice.model.RaffleStatus
import java.time.LocalDateTime
import jakarta.validation.constraints.*

/**
 * Data Transfer Object for Raffle information
 */
data class RaffleDto(
    val id: String,
    val name: String,
    val description: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val maxParticipants: Int,
    val prizeDescription: String,
    val status: RaffleStatus,
    val merkleRoot: String?,
    val externalSeed: String?,
    val winnerEntryId: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Request DTO for creating a new raffle
 */
data class CreateRaffleRequest(
    @field:NotBlank(message = "Name cannot be blank")
    @field:Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    val name: String,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    @field:Future(message = "Start date must be in the future")
    val startDate: LocalDateTime,

    @field:Future(message = "End date must be in the future")
    val endDate: LocalDateTime,

    @field:Min(value = 1, message = "Maximum participants must be at least 1")
    @field:Max(value = 100000, message = "Maximum participants cannot exceed 100,000")
    val maxParticipants: Int = 1000,

    @field:NotBlank(message = "Prize description cannot be blank")
    @field:Size(min = 10, max = 1000, message = "Prize description must be between 10 and 1000 characters")
    val prizeDescription: String
) {
    init {
        require(endDate.isAfter(startDate)) { "End date must be after start date" }
    }
}

/**
 * Request DTO for updating an existing raffle
 */
data class UpdateRaffleRequest(
    val name: String?,
    val description: String?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val maxParticipants: Int?,
    val prizeDescription: String?
)

/**
 * Request DTO for adding a participant to a raffle
 */
data class AddParticipantRequest(
    @field:NotBlank(message = "User ID cannot be blank")
    val userId: String,

    @field:NotBlank(message = "Eligibility proof cannot be blank")
    val eligibilityProof: String
)