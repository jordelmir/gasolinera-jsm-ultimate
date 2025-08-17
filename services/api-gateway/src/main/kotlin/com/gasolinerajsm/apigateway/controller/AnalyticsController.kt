
package com.gasolinerajsm.apigateway.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// DTO for the summary response
data class AnalyticsSummaryDto(
    val totalRevenue: Double,
    val pointsRedeemed: Long,
    val adImpressions: Long
)

@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController {

    @GetMapping("/summary/today")
    fun getTodaySummary(): AnalyticsSummaryDto {
        // TODO: Implement real data aggregation from other services
        // For now, return mock data as per the plan.
        return AnalyticsSummaryDto(
            totalRevenue = 1250.50,
            pointsRedeemed = 3450,
            adImpressions = 12234
        )
    }
}
