package com.gasolinerajsm.raffleservice.application

import com.gasolinerajsm.raffleservice.adapter.out.seed.SeedProvider
import com.gasolinerajsm.raffleservice.domain.model.Raffle
import com.gasolinerajsm.raffleservice.domain.repository.RaffleRepository
import com.gasolinerajsm.raffleservice.domain.repository.PointsLedgerRepository // Assuming this exists from previous tasks
import com.gasolinerajsm.raffleservice.util.HashingUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class RaffleDrawingService(
    private val raffleRepository: RaffleRepository,
    private val seedProvider: SeedProvider,
    private val pointsLedgerRepository: PointsLedgerRepository // Assuming this exists
) {

    @Transactional
    fun drawWinner(raffleId: UUID, blockHeight: Long): Raffle {
        val raffle = raffleRepository.findById(raffleId)
            .orElseThrow { NoSuchElementException("Raffle with ID $raffleId not found") }

        // 1. Get external seed
        val seedValue = seedProvider.getSeed(blockHeight)
        val seedSource = "Bitcoin Block Hash (Height: $blockHeight)"

        // 2. Get all point entries for the period
        // This is a simplification. In a real scenario, you'd filter by raffle period
        // and potentially by points that are eligible for this specific raffle.
        val allPointEntries = pointsLedgerRepository.findAll().map { it.id.toString() } // Using ID as the entry for now

        require(allPointEntries.isNotEmpty()) { "No point entries found to draw a winner." }

        // 3. Calculate winner index
        val combinedHash = HashingUtil.sha256(raffle.merkleRoot, seedValue)
        val winnerIndex = (combinedHash.toBigInteger(16) % allPointEntries.size.toBigInteger()).toInt()

        val winnerPointId = allPointEntries[winnerIndex]

        // 4. Update Raffle entity
        raffle.seedSource = seedSource
        raffle.seedValue = seedValue
        raffle.winnerPointId = winnerPointId
        raffle.status = "DRAWN"
        raffle.drawnAt = Instant.now()

        return raffleRepository.save(raffle)
    }
}
