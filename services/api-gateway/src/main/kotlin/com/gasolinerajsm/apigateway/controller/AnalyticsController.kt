
package com.gasolinerajsm.apigateway.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service // Add this import for @Service annotation

// DTO for the summary response
data class AnalyticsSummaryDto(
    val totalRevenue: Double,
    val pointsRedeemed: Long,
    val adImpressions: Long
)

@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController(
    private val webClientBuilder: WebClient.Builder,
    @Value("${ad-engine.url:http://localhost:8082}") private val adEngineUrl: String,
    @Value("${redemption-service.url:http://localhost:8084}") private val redemptionServiceUrl: String
) {
    private val adEngineClient: WebClient = webClientBuilder.baseUrl(adEngineUrl).build()
    private val redemptionServiceClient: WebClient = webClientBuilder.baseUrl(redemptionServiceUrl).build()

    @GetMapping("/summary/today")
    fun getTodaySummary(): Mono<AnalyticsSummaryDto> {
        val adImpressionsMono: Mono<Long> = adEngineClient.get()
            .uri("/ad/impressions/count") // Assuming an endpoint to get total impressions
            .retrieve()
            .bodyToMono(Long::class.java)
            .onErrorReturn(0L) // Default to 0 if ad-engine is unavailable or errors

        val pointsRedeemedMono: Mono<Long> = redemptionServiceClient.get()
            .uri("/redemptions/points-redeemed/count") // Assuming an endpoint to get total points redeemed
            .retrieve()
            .bodyToMono(Long::class.java)
            .onErrorReturn(0L) // Default to 0 if redemption-service is unavailable or errors

        // For totalRevenue, we'll keep it mock for now as there's no clear service for it
        val totalRevenueMono: Mono<Double> = Mono.just(1250.50)

        return Mono.zip(adImpressionsMono, pointsRedeemedMono, totalRevenueMono)
            .map { tuple ->
                AnalyticsSummaryDto(
                    totalRevenue = tuple.t3,
                    pointsRedeemed = tuple.t2,
                    adImpressions = tuple.t1
                )
            }
    }
}
