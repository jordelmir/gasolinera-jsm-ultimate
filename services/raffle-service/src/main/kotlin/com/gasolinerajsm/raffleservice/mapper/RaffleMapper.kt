package com.gasolinerajsm.raffleservice.mapper

import com.gasolinerajsm.raffleservice.dto.CreateRaffleRequest
import com.gasolinerajsm.raffleservice.dto.RaffleDto
import com.gasolinerajsm.raffleservice.dto.RaffleParticipantDto
import com.gasolinerajsm.raffleservice.dto.RaffleWinnerDto
import com.gasolinerajsm.raffleservice.model.Raffle
import com.gasolinerajsm.raffleservice.model.RaffleParticipant
import com.gasolinerajsm.raffleservice.model.RaffleWinner
import java.util.*

/**
 * Mapper functions for Raffle entity and DTOs
 */

/**
 * Convert Raffle entity to DTO
 */
fun Raffle.toDto(): RaffleDto = RaffleDto(
    id = this.id,
    name = this.name,
    description = this.description,
    startDate = this.startDate,
    endDate = this.endDate,
    maxParticipants = this.maxParticipants,
    prizeDescription = this.prizeDescription,
    status = this.status,
    merkleRoot = this.merkleRoot,
    externalSeed = this.externalSeed,
    winnerEntryId = this.winnerEntryId,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

/**
 * Convert CreateRaffleRequest to Raffle entity
 */
fun CreateRaffleRequest.toEntity(): Raffle = Raffle(
    id = UUID.randomUUID().toString(),
    name = this.name,
    description = this.description,
    startDate = this.startDate,
    endDate = this.endDate,
    maxParticipants = this.maxParticipants,
    prizeDescription = this.prizeDescription
)

/**
 * Convert RaffleParticipant entity to DTO
 */
fun RaffleParticipant.toDto(): RaffleParticipantDto = RaffleParticipantDto(
    id = this.id,
    raffleId = this.raffleId,
    userId = this.userId,
    participationDate = this.participationDate,
    eligibilityProof = this.eligibilityProof,
    entryHash = this.entryHash,
    createdAt = this.createdAt
)

/**
 * Convert RaffleWinner entity to DTO
 */
fun RaffleWinner.toDto(): RaffleWinnerDto = RaffleWinnerDto(
    id = this.id,
    raffleId = this.raffleId,
    participantId = this.participantId,
    selectionDate = this.selectionDate,
    merkleProof = this.merkleProof,
    externalSeed = this.externalSeed,
    selectionIndex = this.selectionIndex,
    verified = this.verified,
    prizeClaimed = this.prizeClaimed,
    claimDate = this.claimDate,
    createdAt = this.createdAt
)