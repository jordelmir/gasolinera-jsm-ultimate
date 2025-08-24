package com.gasolinerajsm.raffleservice.service

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.util.*

/**
 * Service for obtaining external randomness seeds for transparent raffle draws
 */
@Service
class ExternalSeedService(
    private val webClient: WebClient.Builder
) {

    private val logger = LoggerFactory.getLogger(ExternalSeedService::class.java)

    private val bitcoinClient = webClient
        .baseUrl("https://blockchain.info")
        .codecs { it.defaultCodecs().maxInMemorySize(1024 * 1024) }
        .build()

    /**
     * Get external seed from Bitcoin blockchain (latest block hash)
     */
    suspend fun getBitcoinBlockhash(): String? {
        return try {
            logger.info("Fetching latest Bitcoin block hash for external seed")

            val response = bitcoinClient
                .get()
                .uri("/q/latesthash")
                .retrieve()
                .bodyToMono(String::class.java)
                .timeout(Duration.ofSeconds(10))
                .awaitSingleOrNull()

            logger.info("Successfully obtained Bitcoin block hash: {}", response?.take(16) + "...")
            response

        } catch (e: Exception) {
            logger.error("Failed to fetch Bitcoin block hash: {}", e.message)
            null
        }
    }

    /**
     * Get external seed with fallback mechanism
     */
    suspend fun getExternalSeed(): String {
        // Try Bitcoin first
        getBitcoinBlockhash()?.let { bitcoinHash ->
            return "BITCOIN:$bitcoinHash"
        }

        // Fallback to timestamp-based seed
        logger.warn("Using fallback seed generation due to Bitcoin API failure")
        return generateFallbackSeed()
    }

    /**
     * Generate fallback seed when external sources fail
     */
    private fun generateFallbackSeed(): String {
        val timestamp = System.currentTimeMillis()
        val random = UUID.randomUUID().toString()
        return "FALLBACK:$timestamp:$random"
    }

    /**
     * Validate external seed format
     */
    fun validateSeed(seed: String): Boolean {
        return when {
            seed.startsWith("BITCOIN:") -> {
                val hash = seed.removePrefix("BITCOIN:")
                hash.length == 64 && hash.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
            }
            seed.startsWith("FALLBACK:") -> {
                val parts = seed.removePrefix("FALLBACK:").split(":")
                parts.size == 2 && parts[0].toLongOrNull() != null
            }
            else -> false
        }
    }

    /**
     * Get seed source type
     */
    fun getSeedSource(seed: String): SeedSource {
        return when {
            seed.startsWith("BITCOIN:") -> SeedSource.BITCOIN
            seed.startsWith("FALLBACK:") -> SeedSource.FALLBACK
            else -> SeedSource.UNKNOWN
        }
    }
}

/**
 * External seed source types
 */
enum class SeedSource {
    BITCOIN,    // Bitcoin blockchain hash
    FALLBACK,   // Timestamp + UUID fallback
    UNKNOWN     // Unknown or invalid format
}