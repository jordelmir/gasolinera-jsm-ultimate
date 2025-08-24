package com.gasolinerajsm.raffleservice.service

import com.gasolinerajsm.raffleservice.dto.CreateRaffleRequest
import com.gasolinerajsm.raffleservice.dto.RaffleDto
import com.gasolinerajsm.raffleservice.dto.RaffleParticipantDto
import com.gasolinerajsm.raffleservice.dto.RaffleWinnerDto
import com.gasolinerajsm.raffleservice.exception.RaffleNotFoundException
import com.gasolinerajsm.raffleservice.exception.RaffleOperationException
import com.gasolinerajsm.raffleservice.mapper.toDto
import com.gasolinerajsm.raffleservice.mapper.toEntity
import com.gasolinerajsm.raffleservice.model.Raffle
import com.gasolinerajsm.raffleservice.model.RaffleParticipant
import com.gasolinerajsm.raffleservice.model.RaffleStatus
import com.gasolinerajsm.raffleservice.model.RaffleWinner
import com.gasolinerajsm.raffleservice.repository.RaffleParticipantRepository
import com.gasolinerajsm.raffleservice.repository.RaffleRepository
import com.gasolinerajsm.raffleservice.repository.RaffleWinnerRepository
import com.gasolinerajsm.raffleservice.util.MerkleTreeGenerator
import com.gasolinerajsm.raffleservice.util.TransparencyReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * Service class for Raffle operations following hexagonal architecture principles
 */
@Service
@Transactional
class RaffleService(
    private val raffleRepository: RaffleRepository,
    private val participantRepository: RaffleParticipantRepository,
    private val winnerRepository: RaffleWinnerRepository,
    private val externalSeedService: ExternalSeedService
) {

    private val logger = LoggerFactory.getLogger(RaffleService::class.java)

    /**
     * Create a new raffle
     */
    fun createRaffle(request: CreateRaffleRequest): RaffleDto {
        logger.info("Creating new raffle: {}", request.name)

        // Check if raffle with same name already exists and is active
        if (raffleRepository.existsByNameAndStatus(request.name, RaffleStatus.OPEN)) {
            throw RaffleOperationException("Active raffle with name '${request.name}' already exists")
        }

        val raffle = request.toEntity()
        val savedRaffle = raffleRepository.save(raffle)

        logger.info("Raffle created successfully with id: {}", savedRaffle.id)
        return savedRaffle.toDto()
    }

    /**
     * Get raffle by ID
     */
    @Transactional(readOnly = true)
    fun getRaffleById(raffleId: String): RaffleDto {
        logger.debug("Finding raffle with id: {}", raffleId)

        val raffle = raffleRepository.findById(raffleId).orElseThrow {
            RaffleNotFoundException("Raffle not found with id: $raffleId")
        }

        return raffle.toDto()
    }

    /**
     * Get all raffles
     */
    @Transactional(readOnly = true)
    fun getAllRaffles(): List<RaffleDto> {
        logger.debug("Finding all raffles")
        return raffleRepository.findAll().map { it.toDto() }
    }

    /**
     * Get active raffles
     */
    @Transactional(readOnly = true)
    fun getActiveRaffles(): List<RaffleDto> {
        logger.debug("Finding active raffles")
        return raffleRepository.findActiveRaffles().map { it.toDto() }
    }

    /**
     * Get raffles by status
     */
    @Transactional(readOnly = true)
    fun getRafflesByStatus(status: RaffleStatus): List<RaffleDto> {
        logger.debug("Finding raffles with status: {}", status)
        return raffleRepository.findByStatus(status).map { it.toDto() }
    }

    /**
     * Add participant to raffle
     */
    fun addParticipant(raffleId: String, userId: String, eligibilityProof: String): RaffleParticipantDto {
        logger.info("Adding participant {} to raffle {}", userId, raffleId)

        val raffle = raffleRepository.findById(raffleId).orElseThrow {
            RaffleNotFoundException("Raffle not found with id: $raffleId")
        }

        // Check if raffle is active
        if (!raffle.isActive()) {
            throw RaffleOperationException("Raffle is not active for participation")
        }

        // Check if user already participated
        if (participantRepository.existsByRaffleIdAndUserId(raffleId, userId)) {
            throw RaffleOperationException("User already participated in this raffle")
        }

        // Check participant limit
        val currentParticipants = participantRepository.countByRaffleId(raffleId)
        if (currentParticipants >= raffle.maxParticipants) {
            throw RaffleOperationException("Raffle has reached maximum participants limit")
        }

        val participant = RaffleParticipant(
            id = UUID.randomUUID().toString(),
            raffleId = raffleId,
            userId = userId,
            eligibilityProof = eligibilityProof,
            entryHash = MerkleTreeGenerator.sha256("$userId:$raffleId:${LocalDateTime.now()}:$eligibilityProof")
        )

        val savedParticipant = participantRepository.save(participant)
        logger.info("Participant added successfully: {}", savedParticipant.id)

        return savedParticipant.toDto()
    }

    /**
     * Get participants for a raffle
     */
    @Transactional(readOnly = true)
    fun getRaffleParticipants(raffleId: String): List<RaffleParticipantDto> {
        logger.debug("Finding participants for raffle: {}", raffleId)

        if (!raffleRepository.existsById(raffleId)) {
            throw RaffleNotFoundException("Raffle not found with id: $raffleId")
        }

        return participantRepository.findByRaffleId(raffleId).map { it.toDto() }
    }

    /**
     * Execute raffle draw
     */
    suspend fun executeRaffleDraw(raffleId: String): RaffleWinnerDto {
        logger.info("Executing raffle draw for raffle: {}", raffleId)

        return withContext(Dispatchers.IO) {
            val raffle = raffleRepository.findById(raffleId).orElseThrow {
                RaffleNotFoundException("Raffle not found with id: $raffleId")
            }

            // Check if raffle can be drawn
            if (!raffle.canBeDrwan()) {
                throw RaffleOperationException("Raffle cannot be drawn yet or is already completed")
            }

            // Check if winner already exists
            if (winnerRepository.existsByRaffleId(raffleId)) {
                throw RaffleOperationException("Raffle already has a winner")
            }

            // Get all participants
            val participants = participantRepository.findParticipantsForMerkleTree(raffleId)
            if (participants.isEmpty()) {
                throw RaffleOperationException("No participants found for raffle")
            }

            // Generate entry strings for Merkle tree
            val entries = participants.map { it.generateEntryString() }

            // Get external seed
            val externalSeed = externalSeedService.getExternalSeed()

            // Select winner deterministically
            val winnerIndex = MerkleTreeGenerator.selectWinnerIndex(entries, externalSeed)
            val winnerParticipant = participants[winnerIndex]

            // Generate Merkle proof
            val merkleRoot = MerkleTreeGenerator.generateMerkleRoot(entries)
            val merkleProof = MerkleTreeGenerator.generateMerkleProof(entries, entries[winnerIndex])

            // Create winner record
            val winner = RaffleWinner(
                id = UUID.randomUUID().toString(),
                raffleId = raffleId,
                participantId = winnerParticipant.id,
                merkleProof = merkleProof.joinToString(",", "[", "]") { "\"$it\"" },
                externalSeed = externalSeed,
                selectionIndex = winnerIndex
            )

            val savedWinner = winnerRepository.save(winner)

            // Update raffle status
            raffle.setWinner(winnerParticipant.id, merkleRoot, externalSeed)
            raffleRepository.save(raffle)

            // Verify winner selection
            savedWinner.verify()
            winnerRepository.save(savedWinner)

            logger.info("Raffle draw completed successfully. Winner: {}", savedWinner.id)
            savedWinner.toDto()
        }
    }

    /**
     * Get raffle winner
     */
    @Transactional(readOnly = true)
    fun getRaffleWinner(raffleId: String): RaffleWinnerDto? {
        logger.debug("Finding winner for raffle: {}", raffleId)

        if (!raffleRepository.existsById(raffleId)) {
            throw RaffleNotFoundException("Raffle not found with id: $raffleId")
        }

        return winnerRepository.findByRaffleId(raffleId)?.toDto()
    }

    /**
     * Generate transparency report for a raffle
     */
    @Transactional(readOnly = true)
    fun generateTransparencyReport(raffleId: String): TransparencyReport {
        logger.debug("Generating transparency report for raffle: {}", raffleId)

        // Validate that raffle exists
        raffleRepository.findById(raffleId).orElseThrow {
            RaffleNotFoundException("Raffle not found with id: $raffleId")
        }

        val winner = winnerRepository.findByRaffleId(raffleId)
            ?: throw RaffleOperationException("No winner found for raffle")

        val participants = participantRepository.findParticipantsForMerkleTree(raffleId)
        val entries = participants.map { it.generateEntryString() }

        return MerkleTreeGenerator.generateTransparencyReport(
            entries = entries,
            winnerIndex = winner.selectionIndex,
            externalSeed = winner.externalSeed
        )
    }

    /**
     * Close raffle manually
     */
    fun closeRaffle(raffleId: String): RaffleDto {
        logger.info("Closing raffle: {}", raffleId)

        val raffle = raffleRepository.findById(raffleId).orElseThrow {
            RaffleNotFoundException("Raffle not found with id: $raffleId")
        }

        if (raffle.status != RaffleStatus.OPEN) {
            throw RaffleOperationException("Only open raffles can be closed")
        }

        raffle.close()
        val savedRaffle = raffleRepository.save(raffle)

        logger.info("Raffle closed successfully: {}", raffleId)
        return savedRaffle.toDto()
    }

    /**
     * Get raffle statistics
     */
    @Transactional(readOnly = true)
    fun getRaffleStatistics(): Map<String, Long> {
        logger.debug("Getting raffle statistics")

        return mapOf(
            "total" to raffleRepository.count(),
            "open" to raffleRepository.countByStatus(RaffleStatus.OPEN),
            "closed" to raffleRepository.countByStatus(RaffleStatus.CLOSED),
            "completed" to raffleRepository.countByStatus(RaffleStatus.COMPLETED),
            "cancelled" to raffleRepository.countByStatus(RaffleStatus.CANCELLED),
            "totalParticipants" to participantRepository.count(),
            "totalWinners" to winnerRepository.count(),
            "verifiedWinners" to winnerRepository.countByVerifiedTrue(),
            "unclaimedPrizes" to winnerRepository.countByPrizeClaimedFalse()
        )
    }
}