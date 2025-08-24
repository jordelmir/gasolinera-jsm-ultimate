package com.gasolinerajsm.raffleservice.util

import java.security.MessageDigest

/**
 * Merkle Tree Generator for transparent and verifiable raffle draws
 * Provides cryptographic proof of fair winner selection
 */
object MerkleTreeGenerator {

    /**
     * Generate Merkle root from list of entries
     */
    fun generateMerkleRoot(entries: List<String>): String {
        if (entries.isEmpty()) {
            return sha256("")
        }

        if (entries.size == 1) {
            return sha256(entries[0])
        }

        // Hash all entries first
        var currentLevel = entries.map { sha256(it) }

        // Build tree level by level
        while (currentLevel.size > 1) {
            val nextLevel = mutableListOf<String>()

            // Process pairs
            for (i in currentLevel.indices step 2) {
                val left = currentLevel[i]
                val right = if (i + 1 < currentLevel.size) {
                    currentLevel[i + 1]
                } else {
                    left // Duplicate last node if odd number
                }

                nextLevel.add(sha256(left + right))
            }

            currentLevel = nextLevel
        }

        return currentLevel[0]
    }

    /**
     * Generate Merkle proof for a specific entry
     */
    fun generateMerkleProof(entries: List<String>, targetEntry: String): List<String> {
        if (entries.isEmpty() || !entries.contains(targetEntry)) {
            return emptyList()
        }

        val targetIndex = entries.indexOf(targetEntry)
        val proof = mutableListOf<String>()

        // Hash all entries first
        var currentLevel = entries.map { sha256(it) }
        var currentIndex = targetIndex

        // Build proof by collecting sibling hashes at each level
        while (currentLevel.size > 1) {
            val siblingIndex = if (currentIndex % 2 == 0) {
                currentIndex + 1
            } else {
                currentIndex - 1
            }

            // Add sibling hash to proof (if exists)
            if (siblingIndex < currentLevel.size) {
                proof.add(currentLevel[siblingIndex])
            } else {
                // Duplicate current node if no sibling
                proof.add(currentLevel[currentIndex])
            }

            // Move to next level
            val nextLevel = mutableListOf<String>()
            for (i in currentLevel.indices step 2) {
                val left = currentLevel[i]
                val right = if (i + 1 < currentLevel.size) {
                    currentLevel[i + 1]
                } else {
                    left
                }
                nextLevel.add(sha256(left + right))
            }

            currentLevel = nextLevel
            currentIndex = currentIndex / 2
        }

        return proof
    }

    /**
     * Verify Merkle proof for a given entry
     */
    fun verifyMerkleProof(
        entry: String,
        proof: List<String>,
        root: String,
        index: Int
    ): Boolean {
        var currentHash = sha256(entry)
        var currentIndex = index

        for (siblingHash in proof) {
            currentHash = if (currentIndex % 2 == 0) {
                // Current node is left child
                sha256(currentHash + siblingHash)
            } else {
                // Current node is right child
                sha256(siblingHash + currentHash)
            }
            currentIndex = currentIndex / 2
        }

        return currentHash == root
    }

    /**
     * Select winner deterministically using external seed
     */
    fun selectWinnerIndex(entries: List<String>, externalSeed: String): Int {
        if (entries.isEmpty()) {
            throw IllegalArgumentException("Cannot select winner from empty entries")
        }

        // Combine merkle root with external seed for deterministic selection
        val merkleRoot = generateMerkleRoot(entries)
        val combinedSeed = sha256(merkleRoot + externalSeed)

        // Convert hash to number and mod by entries size
        val hashBytes = combinedSeed.take(16) // Use first 16 hex chars (64 bits)
        val seedNumber = hashBytes.toLong(16)

        return (seedNumber % entries.size).toInt().let {
            if (it < 0) it + entries.size else it
        }
    }

    /**
     * Generate transparency report for a raffle
     */
    fun generateTransparencyReport(
        entries: List<String>,
        winnerIndex: Int,
        externalSeed: String
    ): TransparencyReport {
        val merkleRoot = generateMerkleRoot(entries)
        val winnerEntry = entries[winnerIndex]
        val merkleProof = generateMerkleProof(entries, winnerEntry)

        return TransparencyReport(
            totalEntries = entries.size,
            merkleRoot = merkleRoot,
            externalSeed = externalSeed,
            winnerIndex = winnerIndex,
            winnerEntry = winnerEntry,
            merkleProof = merkleProof,
            verificationPassed = verifyMerkleProof(winnerEntry, merkleProof, merkleRoot, winnerIndex)
        )
    }

    /**
     * SHA-256 hash function
     */
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Transparency report data class
 */
data class TransparencyReport(
    val totalEntries: Int,
    val merkleRoot: String,
    val externalSeed: String,
    val winnerIndex: Int,
    val winnerEntry: String,
    val merkleProof: List<String>,
    val verificationPassed: Boolean
)