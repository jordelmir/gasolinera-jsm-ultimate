package com.gasolinerajsm.apigateway.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * Enhanced fallback controller for circuit breaker patterns
 * Provides graceful degradation when services are unavailable
 */
@RestController
@RequestMapping("/fallback")
class FallbackController {

    private val logger = LoggerFactory.getLogger(FallbackController::class.java)

    /**
     * Fallback for Auth Service
     */
    @RequestMapping("/auth/**")
    fun authServiceFallback(): Mono<ResponseEntity<FallbackResponse>> {
        logger.warn("Auth service is unavailable, returning fallback response")

        val response = FallbackResponse(
            error = "SERVICE_UNAVAILABLE",
            message = "Authentication service is temporarily unavailable. Please try again later.",
            service = "auth-service",
            timestamp = LocalDateTime.now(),
            statusCode = HttpStatus.SERVICE_UNAVAILABLE.value(),
            fallbackData = mapOf(
                "suggestion" to "Please check your connection and try again in a few minutes",
                "supportContact" to "support@gasolinerajsm.com"
            )
        )

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response))
    }

    /**
     * Fallback for Station Service
     */
    @RequestMapping("/stations/**")
    fun stationServiceFallback(): Mono<ResponseEntity<FallbackResponse>> {
        logger.warn("Station service is unavailable, returning fallback response")

        val response = FallbackResponse(
            error = "SERVICE_UNAVAILABLE",
            message = "Station management service is temporarily unavailable",
            service = "station-service",
            timestamp = LocalDateTime.now(),
            statusCode = HttpStatus.SERVICE_UNAVAILABLE.value(),
            fallbackData = mapOf(
                "stations" to emptyList<Any>(),
                "message" to "Station data is temporarily unavailable. Please try again later."
            )
        )

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response))
    }

    /**
     * Fallback for Coupon Service
     */
    @RequestMapping("/coupons/**")
    fun couponServiceFallback(): Mono<ResponseEntity<FallbackResponse>> {
        logger.warn("Coupon service is unavailable, returning fallback response")

        val response = FallbackResponse(
            error = "SERVICE_UNAVAILABLE",
            message = "Coupon service is temporarily unavailable",
            service = "coupon-service",
            timestamp = LocalDateTime.now(),
            statusCode = HttpStatus.SERVICE_UNAVAILABLE.value(),
            fallbackData = mapOf(
                "message" to "Unable to generate or validate coupons at this time",
                "alternativeAction" to "Please contact station staff for manual processing"
            )
        )

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response))
    }

    /**
     * Fallback for Redemption Service
     */
    @RequestMapping("/redemptions/**")
    fun redemptionServiceFallback(): Mono<ResponseEntity<FallbackResponse>> {
        logger.warn("Redemption service is unavailable, returning fallback response")

        val response = FallbackResponse(
            error = "SERVICE_UNAVAILABLE",
            message = "Redemption service is temporarily unavailable",
            service = "redemption-service",
            timestamp = LocalDateTime.now(),
            statusCode = HttpStatus.SERVICE_UNAVAILABLE.value(),
            fallbackData = mapOf(
                "redemptions" to emptyList<Any>(),
                "message" to "Points and rewards processing is temporarily unavailable"
            )
        )

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response))
    }

    /**
     * Fallback for Ad Engine Service
     */
    @RequestMapping("/ads/**")
    fun adEngineFallback(): Mono<ResponseEntity<FallbackResponse>> {
        logger.warn("Ad Engine service is unavailable, returning fallback response")

        val response = FallbackResponse(
            error = "SERVICE_UNAVAILABLE",
            message = "Advertisement service is temporarily unavailable",
            service = "ad-engine",
            timestamp = LocalDateTime.now(),
            statusCode = HttpStatus.SERVICE_UNAVAILABLE.value(),
            fallbackData = mapOf(
                "ads" to emptyList<Any>(),
                "campaigns" to emptyList<Any>(),
                "message" to "No advertisements available at this time"
            )
        )

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response))
    }

    /**
     * Fallback for Raffle Service
     */
    @RequestMapping("/raffles/**")
    fun raffleServiceFallback(): Mono<ResponseEntity<FallbackResponse>> {
        logger.warn("Raffle service is unavailable, returning fallback response")

        val response = FallbackResponse(
            error = "SERVICE_UNAVAILABLE",
            message = "Raffle service is temporarily unavailable",
            service = "raffle-service",
            timestamp = LocalDateTime.now(),
            statusCode = HttpStatus.SERVICE_UNAVAILABLE.value(),
            fallbackData = mapOf(
                "raffles" to emptyList<Any>(),
                "activeRaffles" to 0,
                "message" to "Raffle participation is temporarily unavailable"
            )
        )

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response))
    }

    /**
     * Generic fallback for any unhandled service
     */
    @RequestMapping("/generic/**")
    fun genericFallback(): Mono<ResponseEntity<FallbackResponse>> {
        logger.warn("Unknown service is unavailable, returning generic fallback response")

        val response = FallbackResponse(
            error = "SERVICE_UNAVAILABLE",
            message = "The requested service is temporarily unavailable",
            service = "unknown",
            timestamp = LocalDateTime.now(),
            statusCode = HttpStatus.SERVICE_UNAVAILABLE.value(),
            fallbackData = mapOf(
                "message" to "Please try again later or contact support if the problem persists"
            )
        )

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response))
    }

    /**
     * Health check endpoint for the fallback controller
     */
    @GetMapping("/health")
    fun health(): Mono<ResponseEntity<Map<String, Any>>> {
        val healthData = mapOf(
            "status" to "UP",
            "service" to "api-gateway-fallback",
            "timestamp" to LocalDateTime.now(),
            "message" to "Fallback controller is operational"
        )

        return Mono.just(ResponseEntity.ok(healthData))
    }

    /**
     * Endpoint to get fallback statistics
     */
    @GetMapping("/stats")
    fun getFallbackStats(): Mono<ResponseEntity<Map<String, Any>>> {
        // In a real implementation, you would track these metrics
        val stats = mapOf(
            "totalFallbacks" to 0,
            "serviceBreakdowns" to mapOf(
                "auth-service" to 0,
                "station-service" to 0,
                "coupon-service" to 0,
                "redemption-service" to 0,
                "ad-engine" to 0,
                "raffle-service" to 0
            ),
            "lastReset" to LocalDateTime.now(),
            "uptime" to "100%"
        )

        return Mono.just(ResponseEntity.ok(stats))
    }
}

/**
 * Enhanced fallback response data class
 */
data class FallbackResponse(
    val error: String,
    val message: String,
    val service: String,
    val timestamp: LocalDateTime,
    val statusCode: Int,
    val fallbackData: Map<String, Any> = emptyMap(),
    val requestId: String? = null,
    val retryAfter: Int? = 30 // seconds
)