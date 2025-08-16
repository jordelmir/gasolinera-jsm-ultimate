package com.gasolinerajsm.raffleservice.adapter.out.seed

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Service
class BitcoinSeedProvider(
    private val restTemplate: RestTemplate // Assuming RestTemplate is configured as a Bean
) : SeedProvider {

    private val BITCOIN_API_URL = "https://blockchain.info/block-height/{blockHeight}?format=json"

    override fun getSeed(blockHeight: Long): String {
        val url = BITCOIN_API_URL.replace("{blockHeight}", blockHeight.toString())
        val response = restTemplate.getForObject<BitcoinBlockResponse>(url)
        return response.blocks.first().hash
    }
}

data class BitcoinBlockResponse(
    val blocks: List<BlockInfo>
)

data class BlockInfo(
    val hash: String
)
