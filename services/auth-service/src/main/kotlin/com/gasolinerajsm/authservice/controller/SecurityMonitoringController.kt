package com.gasolinerajsm.authservice.controller

import com.gasolinerajsm.authservice.monitoring.SecurityMetrics
import com.gasolinerajsm.authservice.monitoring.SecurityMonitor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for security monitoring and metrics endpoints.
 * These endpoints provide insights into authentication patterns and security events.
 */
@RestController
@RequestMapping("/auth/monitoring")
@Tag(name = "Security Monitoring", description = "Security monitoring and metrics endpoints")
class SecurityMonitoringController(
    private val securityMonitor: SecurityMonitor
) {

    @Operation(
        summary = "Get Security Metrics",
        description = "Retrieves current security metrics including authentication success/failure rates, lockouts, and suspicious activities",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Security metrics retrieved successfully",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = SecurityMetrics::class)
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid admin token required",
            content = [Content(schema = Schema(implementation = Map::class))]
        )
    ])
    @GetMapping("/metrics", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSecurityMetrics(): ResponseEntity<SecurityMetrics> {
        val metrics = securityMonitor.getSecurityMetrics()
        return ResponseEntity.ok(metrics)
    }

    @Operation(
        summary = "Get Security Health Status",
        description = "Provides a simplified health check for security monitoring systems",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Security health status retrieved successfully",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = Map::class)
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid admin token required",
            content = [Content(schema = Schema(implementation = Map::class))]
        )
    ])
    @GetMapping("/health", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSecurityHealth(): ResponseEntity<Map<String, Any>> {
        val metrics = securityMonitor.getSecurityMetrics()

        // Calculate health indicators
        val totalAttempts = metrics.otpVerificationSuccess + metrics.otpVerificationFailure
        val successRate = if (totalAttempts > 0) {
            (metrics.otpVerificationSuccess.toDouble() / totalAttempts * 100)
        } else {
            100.0
        }

        val healthStatus = when {
            successRate >= 90 && metrics.suspiciousActivities == 0 -> "HEALTHY"
            successRate >= 70 && metrics.suspiciousActivities < 5 -> "WARNING"
            else -> "CRITICAL"
        }

        val healthData = mapOf(
            "status" to healthStatus,
            "successRate" to String.format("%.2f%%", successRate),
            "totalAuthAttempts" to totalAttempts,
            "suspiciousActivities" to metrics.suspiciousActivities,
            "accountLockouts" to metrics.accountLockouts,
            "averageAuthTime" to String.format("%.2fms", metrics.averageAuthTime),
            "timestamp" to System.currentTimeMillis()
        )

        return ResponseEntity.ok(healthData)
    }

    @Operation(
        summary = "Get Authentication Statistics",
        description = "Provides detailed authentication statistics for monitoring dashboards",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Authentication statistics retrieved successfully",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = Map::class)
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid admin token required",
            content = [Content(schema = Schema(implementation = Map::class))]
        )
    ])
    @GetMapping("/stats", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAuthenticationStats(): ResponseEntity<Map<String, Any>> {
        val metrics = securityMonitor.getSecurityMetrics()

        val totalAttempts = metrics.otpVerificationSuccess + metrics.otpVerificationFailure
        val successRate = if (totalAttempts > 0) {
            (metrics.otpVerificationSuccess.toDouble() / totalAttempts * 100)
        } else {
            100.0
        }

        val failureRate = if (totalAttempts > 0) {
            (metrics.otpVerificationFailure.toDouble() / totalAttempts * 100)
        } else {
            0.0
        }

        val lockoutRate = if (metrics.otpGenerated > 0) {
            (metrics.accountLockouts.toDouble() / metrics.otpGenerated * 100)
        } else {
            0.0
        }

        val stats = mapOf(
            "authentication" to mapOf(
                "totalAttempts" to totalAttempts,
                "successfulAttempts" to metrics.otpVerificationSuccess,
                "failedAttempts" to metrics.otpVerificationFailure,
                "successRate" to String.format("%.2f%%", successRate),
                "failureRate" to String.format("%.2f%%", failureRate)
            ),
            "otp" to mapOf(
                "generated" to metrics.otpGenerated,
                "averageAuthTime" to String.format("%.2fms", metrics.averageAuthTime)
            ),
            "security" to mapOf(
                "accountLockouts" to metrics.accountLockouts,
                "lockoutRate" to String.format("%.2f%%", lockoutRate),
                "tokenRevocations" to metrics.tokenRevocations,
                "invalidPhoneAttempts" to metrics.invalidPhoneAttempts,
                "suspiciousActivities" to metrics.suspiciousActivities
            ),
            "timestamp" to System.currentTimeMillis()
        )

        return ResponseEntity.ok(stats)
    }
}