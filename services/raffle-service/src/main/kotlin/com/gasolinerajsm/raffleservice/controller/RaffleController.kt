package com.gasolinerajsm.raffleservice.controller

import com.gasolinerajsm.raffleservice.dto.AddParticipantRequest
import com.gasolinerajsm.raffleservice.dto.CreateRaffleRequest
import com.gasolinerajsm.raffleservice.dto.RaffleDto
import com.gasolinerajsm.raffleservice.dto.RaffleParticipantDto
import com.gasolinerajsm.raffleservice.dto.RaffleWinnerDto
import com.gasolinerajsm.raffleservice.model.RaffleStatus
import com.gasolinerajsm.raffleservice.service.RaffleService
import com.gasolinerajsm.raffleservice.util.TransparencyReport
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

/**
 * REST Controller for Raffle operations
 */
@RestController
@RequestMapping("/api/v1/raffles")
@CrossOrigin(origins = ["*"]) // Configure properly for production
class RaffleController(private val raffleService: RaffleService) {

    private val logger = LoggerFactory.getLogger(RaffleController::class.java)

    /**
     * Get all raffles
     */
    @GetMapping
    fun getAllRaffles(
        @RequestParam(required = false) status: RaffleStatus?
    ): ResponseEntity<List<RaffleDto>> {
        logger.debug("GET /api/v1/raffles - status: {}", status)

        val raffles = if (status != null) {
            raffleService.getRafflesByStatus(status)
        } else {
            raffleService.getAllRaffles()
        }

        return ResponseEntity.ok(raffles)
    }

    /**
     * Get active raffles
     */
    @GetMapping("/active")
    fun getActiveRaffles(): ResponseEntity<List<RaffleDto>> {
        logger.debug("GET /api/v1/raffles/active")

        val raffles = raffleService.getActiveRaffles()
        return ResponseEntity.ok(raffles)
    }

    /**
     * Get raffle by ID
     */
    @GetMapping("/{id}")
    fun getRaffleById(@PathVariable id: String): ResponseEntity<RaffleDto> {
        logger.debug("GET /api/v1/raffles/{}", id)

        val raffle = raffleService.getRaffleById(id)
        return ResponseEntity.ok(raffle)
    }

    /**
     * Create new raffle
     */
    @PostMapping
    fun createRaffle(@Valid @RequestBody request: CreateRaffleRequest): ResponseEntity<RaffleDto> {
        logger.info("POST /api/v1/raffles - Creating raffle: {}", request.name)

        val createdRaffle = raffleService.createRaffle(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRaffle)
    }

    /**
     * Add participant to raffle
     */
    @PostMapping("/{id}/participants")
    fun addParticipant(
        @PathVariable id: String,
        @Valid @RequestBody request: AddParticipantRequest
    ): ResponseEntity<RaffleParticipantDto> {
        logger.info("POST /api/v1/raffles/{}/participants - Adding participant: {}", id, request.userId)

        val participant = raffleService.addParticipant(id, request.userId, request.eligibilityProof)
        return ResponseEntity.status(HttpStatus.CREATED).body(participant)
    }

    /**
     * Get raffle participants
     */
    @GetMapping("/{id}/participants")
    fun getRaffleParticipants(@PathVariable id: String): ResponseEntity<List<RaffleParticipantDto>> {
        logger.debug("GET /api/v1/raffles/{}/participants", id)

        val participants = raffleService.getRaffleParticipants(id)
        return ResponseEntity.ok(participants)
    }

    /**
     * Execute raffle draw
     */
    @PostMapping("/{id}/draw")
    fun executeRaffleDraw(@PathVariable id: String): ResponseEntity<RaffleWinnerDto> {
        logger.info("POST /api/v1/raffles/{}/draw - Executing draw", id)

        val winner = runBlocking {
            raffleService.executeRaffleDraw(id)
        }

        return ResponseEntity.ok(winner)
    }

    /**
     * Get raffle winner
     */
    @GetMapping("/{id}/winner")
    fun getRaffleWinner(@PathVariable id: String): ResponseEntity<RaffleWinnerDto?> {
        logger.debug("GET /api/v1/raffles/{}/winner", id)

        val winner = raffleService.getRaffleWinner(id)
        return ResponseEntity.ok(winner)
    }

    /**
     * Get transparency report
     */
    @GetMapping("/{id}/transparency")
    fun getTransparencyReport(@PathVariable id: String): ResponseEntity<TransparencyReport> {
        logger.debug("GET /api/v1/raffles/{}/transparency", id)

        val report = raffleService.generateTransparencyReport(id)
        return ResponseEntity.ok(report)
    }

    /**
     * Close raffle manually
     */
    @PostMapping("/{id}/close")
    fun closeRaffle(@PathVariable id: String): ResponseEntity<RaffleDto> {
        logger.info("POST /api/v1/raffles/{}/close - Closing raffle", id)

        val closedRaffle = raffleService.closeRaffle(id)
        return ResponseEntity.ok(closedRaffle)
    }

    /**
     * Get raffle statistics
     */
    @GetMapping("/stats")
    fun getRaffleStatistics(): ResponseEntity<Map<String, Long>> {
        logger.debug("GET /api/v1/raffles/stats")

        val stats = raffleService.getRaffleStatistics()
        return ResponseEntity.ok(stats)
    }
}