package com.gasolinerajsm.raffleservice.service

import com.gasolinerajsm.raffleservice.model.Raffle
import com.gasolinerajsm.raffleservice.model.RaffleEntry
import com.gasolinerajsm.raffleservice.model.RaffleStatus
import com.gasolinerajsm.raffleservice.model.RaffleWinner
import com.gasolinerajsm.raffleservice.repository.RaffleEntryRepository
import com.gasolinerajsm.raffleservice.repository.RaffleRepository
import com.gasolinerajsm.raffleservice.repository.RaffleWinnerRepository
import com.gasolinerajsm.raffleservice.util.MerkleTreeGenerator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigInteger
import java.time.LocalDateTime

@Service
class RaffleService(
    private val raffleRepository: RaffleRepository,
    private val raffleEntryRepository: RaffleEntryRepository,
    private val raffleWinnerRepository: RaffleWinnerRepository,
    private val webClientBuilder: WebClient.Builder
) {

    private val logger = LoggerFactory.getLogger(RaffleService::class.java)

    @Transactional
    fun closeRafflePeriod(period: String): Raffle {
        logger.info("Attempting to close raffle period: {}", period)

        val existingRaffle = raffleRepository.findByPeriod(period)
        if (existingRaffle != null && existingRaffle.status != RaffleStatus.OPEN) {
            throw IllegalStateException("Raffle for period $period is already closed or drawn.")
        }

        // TODO: Fetch actual point IDs from redemption-service for the given period
        // For now, simulate fetching point IDs
        val pointIds = (1..100).map { "point_id_${it}_$period" }
        if (pointIds.isEmpty()) {
            throw IllegalStateException("No points found for period $period. Cannot close raffle.")
        }
        logger.info("Fetched {} point IDs for period {}", pointIds.size, period)

        val merkleRoot = MerkleTreeGenerator.generateMerkleRoot(pointIds)
        logger.info("Generated Merkle Root for period {}: {}", period, merkleRoot)

        val raffle = existingRaffle ?: Raffle(period = period, merkleRoot = merkleRoot)
        raffle.merkleRoot = merkleRoot // Update if already exists
        raffle.status = RaffleStatus.CLOSED
        val savedRaffle = raffleRepository.save(raffle)
        logger.info("Raffle for period {} closed with ID: {}", period, savedRaffle.id)

        // Save raffle entries
        pointIds.forEach { pointId ->
            raffleEntryRepository.save(RaffleEntry(raffleId = savedRaffle.id!!, pointId = pointId))
        }
        logger.info("Saved {} raffle entries for raffle ID: {}", pointIds.size, savedRaffle.id)

        return savedRaffle
    }

    @Transactional
    fun executeRaffleDraw(raffleId: Long): RaffleWinner {
        logger.info("Attempting to execute draw for raffle ID: {}", raffleId)
        val raffle = raffleRepository.findById(raffleId)
            .orElseThrow { IllegalArgumentException("Raffle with ID $raffleId not found.") }

        if (raffle.status != RaffleStatus.CLOSED) {
            throw IllegalStateException("Raffle with ID $raffleId is not in CLOSED status. Current status: ${raffle.status}")
        }

        val externalSeed = getBitcoinBlockhash().block() // Blocking call for simplicity in demo
        if (externalSeed == null) {
            throw IllegalStateException("Could not retrieve external seed for draw.")
        }
        logger.info("Retrieved external seed for raffle ID {}: {}", raffleId, externalSeed)

        val entries = raffleEntryRepository.findByRaffleId(raffleId)
        if (entries.isEmpty()) {
            throw IllegalStateException("No entries found for raffle ID $raffleId. Cannot draw winner.")
        }

        val winnerIndex = selectWinnerDeterministically(raffle.merkleRoot, externalSeed, entries.size)
        val winningEntry = entries[winnerIndex]
        logger.info("Selected winning entry for raffle ID {}: Index {}, Point ID {}", raffleId, winnerIndex, winningEntry.pointId)

        val winner = RaffleWinner(
            raffleId = raffle.id!!,
            userId = "mock-user-id-from-point", // TODO: Extract user ID from pointId or fetch from redemption-service
            winningPointId = winningEntry.pointId,
            prize = "10000 Puntos G" // Example prize
        )
        val savedWinner = raffleWinnerRepository.save(winner)

        raffle.status = RaffleStatus.DRAWN
        raffle.drawAt = LocalDateTime.now()
        raffle.externalSeed = externalSeed
        raffle.winnerEntryId = winningEntry.pointId
        raffleRepository.save(raffle)
        logger.info("Raffle ID {} drawn. Winner: {}", raffleId, savedWinner.winningPointId)

        return savedWinner
    }

    private fun getBitcoinBlockhash(): Mono<String> {
        // Using a public API for Bitcoin block hash
        val webClient = webClientBuilder.baseUrl("https://blockchain.info").build()
        return webClient.get()
            .uri("/q/latesthash")
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnError { e -> logger.error("Error fetching Bitcoin block hash: {}", e.message) }
    }

    private fun selectWinnerDeterministically(merkleRoot: String, seed: String, numberOfEntries: Int): Int {
        // Combine Merkle Root and external seed
        val combinedHash = MerkleTreeGenerator.sha256(merkleRoot + seed)
        
        // Convert hash to a large integer
        val bigInt = BigInteger(combinedHash, 16)

        // Use modulo to get a deterministic index
        return bigInt.mod(BigInteger.valueOf(numberOfEntries.toLong())).toInt()
    }
}