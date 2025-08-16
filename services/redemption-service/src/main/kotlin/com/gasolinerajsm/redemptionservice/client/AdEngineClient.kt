package com.gasolinerajsm.redemptionservice.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

data class AdSelectionRequest(val userId: Long, val stationId: Long)
data class AdCreativeResponse(val adUrl: String, val campaignId: Long, val creativeId: String)

@Component
class AdEngineClient(
    private val restTemplate: RestTemplate,
    @Value("\${ad.engine.url}")
    private val adEngineUrl: String
) {

    fun selectAd(request: AdSelectionRequest): AdCreativeResponse? {
        val url = "$adEngineUrl/ad/select"
        return restTemplate.postForObject(url, request, AdCreativeResponse::class.java)
    }

    fun recordImpression(userId: Long, campaignId: Long, creativeId: String) {
        val url = "$adEngineUrl/ad/impression"
        val impressionRequest = mapOf(
            "userId" to userId,
            "campaignId" to campaignId,
            "creativeId" to creativeId
        )
        restTemplate.postForEntity(url, impressionRequest, Void::class.java)
    }
}
