
package com.gasolinerajsm.raffleservice.service

import com.gasolinerajsm.raffleservice.controller.RaffleWinner
import com.gasolinerajsm.raffleservice.util.MerkleTree
import org.springframework.stereotype.Service
import java.math.BigInteger
import com.google.common.hash.Hashing
import java.nio.charset.StandardCharsets

@Service
class RaffleService(
    // Inject repositories
) {

    fun createRaffleForCurrentPeriod(): String {
        // 1. Fetch all eligible points from the points table (this would be a gRPC call or DB query)
        val entries = listOf("point_id_1", "point_id_2", "point_id_3") // Placeholder

        // 2. Build Merkle Tree
        val merkleTree = MerkleTree.build(entries)
        val root = merkleTree.root

        // 3. Save raffle and entries to DB
        println("Created raffle with Merkle Root: $root")
        return root
    }

    fun drawWinner(raffleId: java.util.UUID, seed: String?): RaffleWinner {
        val finalSeed = seed ?: "fetch_from_external_source" // e.g., Bitcoin block hash
        
        // 1. Fetch raffle from DB
        val merkleRoot = "merkle_root_from_db" // Placeholder
        val totalEntries = 1000L // Placeholder

        // 2. Determine winning index
        val combined = merkleRoot + finalSeed
        val hash = Hashing.sha256().hashString(combined, StandardCharsets.UTF_8).asBytes()
        val winningIndex = BigInteger(1, hash).mod(BigInteger.valueOf(totalEntries)).toLong()

        // 3. Find the winner at that index and save
        println("Winning index is $winningIndex")
        return RaffleWinner("user_id_placeholder", "point_id_placeholder")
    }

    fun createCarRaffle(): String {
        // 1. Fetch all eligible points from the points table (this would be a gRPC call or DB query)
        val entries = listOf("point_id_1", "point_id_2", "point_id_3") // Placeholder

        // 2. Build Merkle Tree
        val merkleTree = MerkleTree.build(entries)
        val root = merkleTree.root

        // 3. Save raffle and entries to DB
        println("Created car raffle with Merkle Root: $root")
        return root
    }
}
