package com.gasolinerajsm.raffleservice.application

import com.gasolinerajsm.raffleservice.domain.model.Raffle
import com.gasolinerajsm.raffleservice.domain.repository.RaffleRepository
import com.gasolinerajsm.raffleservice.domain.service.MerkleTree
import org.springframework.stereotype.Service

@Service
class RaffleCreationService(
    private val raffleRepository: RaffleRepository
) {

    fun createRaffle(period: String, pointEntries: List<String>): Raffle {
        require(pointEntries.isNotEmpty()) { "Point entries cannot be empty for raffle creation." }
        val merkleTree = MerkleTree(pointEntries)
        val merkleRoot = merkleTree.root

        val raffle = Raffle(
            period = period,
            merkleRoot = merkleRoot,
            status = "CREATED"
        )
        return raffleRepository.save(raffle)
    }
}
