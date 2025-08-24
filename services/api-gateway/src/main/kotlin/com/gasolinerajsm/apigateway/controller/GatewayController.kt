package com.gasolinerajsm.apigateway.controller

import com.gasolinerajsm.apigateway.config.JwtConfig
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * Main gateway controller for utility endpoints
 */
@RestController
@RequestMapping("/api/gateway")
class GatewayController(
    private val jwtConfig: JwtConfig
) {

    private val logger = LoggerFactory.getLogger(GatewayController::class.java)

    /**
     * Validate JWT token endpoint
     */
    @PostMapping("/validate-token")
    fun validateToken(@RequestHeader("Authorization") authHeader: String): Mono<ResponseEntity<TokenValidationResponse>> {
        return try {
            if (!authHeader.startsWith("Bearer ")) {
                return Mono.just(ResponseEntity.badRequest().body(
                    TokenValidationResponse(
                        valid = false,
                        message = "Invalid Authorization header format",
                        timestamp = LocalDateTime.now()
                    )
                ))
            }

            val token = authHeader.substring(7)
            val isValid = jwtConfig.validateToken(token)

            if (isValid) {
                val username = jwtConfig.extractUsername(token)
                val userId = jwtConfig.extractUserId(token)
                val roles = jwtConfig.extractRoles(token)
                val userType = jwtConfig.extractUserType(token)

                Mono.just(ResponseEntity.ok(
                    TokenValidationResponse(
                        valid = true,
                        message = "Token is valid",
                        timestamp = LocalDateTime.now(),
                        userInfo = mapOf(
                            "username" to username,
                            "userId" to userId,
                            "roles" to roles,
                            "userType" to userType
                        )
                    )
                ))
            } else {
                Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    TokenValidationResponse(
                        valid = false,
                        message = "Token is invalid or expired",
                        timestamp = LocalDateTime.now()
                    )
                ))
            }
        } catch (e: Exception) {
            logger.error("Error validating token", e)
            Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                TokenValidationResponse(
                    valid = false,
                    message = "Error validating token: ${e.message}",
                    timestamp = LocalDateTime.now()
                )
            ))
        }
    }

    /**
     * Gateway health check
     */
    @GetMapping("/health")
    fun health(): Mono<ResponseEntity<GatewayHealthResponse>> {
        val healthData = GatewayHealthResponse(
            status = "UP",
            timestamp = LocalDateTime.now(),
            version = "1.0.0",
            services = mapOf(
                "auth-service" to "UNKNOWN",
                "station-service" to "UNKNOWN",
                "coupon-service" to "UNKNOWN",
                "redemption-service" to "UNKNOWN",
                "ad-engine" to "UNKNOWN",
                "raffle-service" to "UNKNOWN"
            ),
            features = mapOf(
                "jwt-authentication" to true,
                "rate-limiting" to true,
                "circuit-breakers" to true,
                "request-logging" to true
            )
        )

        return Mono.just(ResponseEntity.ok(healthData))
    }

    /**
     * Gateway configuration info
     */
    @GetMapping("/info")
    fun info(): Mono<ResponseEntity<GatewayInfoResponse>> {
        val infoData = GatewayInfoResponse(
            name = "Gasolinera JSM API Gateway",
            version = "1.0.0",
            description = "Central API Gateway for Gasolinera JSM microservices",
            timestamp = LocalDateTime.now(),
            routes = listOf(
                "/api/auth/** -> auth-service",
                "/api/stations/** -> station-service",
                "/api/coupons/** -> coupon-service",
                "/api/redemptions/** -> redemption-service",
                "/api/ads/** -> ad-engine",
                "/api/raffles/** -> raffle-service"
            ),
            features = listOf(
                "JWT Authentication",
                "Rate Limiting",
                "Circuit Breakers",
                "Request/Response Logging",
                "CORS Support",
                "Fallback Responses"
            )
        )

        return Mono.just(ResponseEntity.ok(infoData))
    }

    /**
     * Get current user info from JWT token
     */
    @GetMapping("/user-info")
    fun getUserInfo(@RequestHeader("Authorization") authHeader: String): Mono<ResponseEntity<UserInfoResponse>> {
        return try {
            if (!authHeader.startsWith("Bearer ")) {
                return Mono.just(ResponseEntity.badRequest().body(
                    UserInfoResponse(
                        success = false,
                        message = "Invalid Authorization header format",
                        timestamp = LocalDateTime.now()
                    )
                ))
            }

            val token = authHeader.substring(7)

            if (!jwtConfig.validateToken(token)) {
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    UserInfoResponse(
                        success = false,
                        message = "Invalid or expired token",
                        timestamp = LocalDateTime.now()
                    )
                ))
            }

            val username = jwtConfig.extractUsername(token)
            val userId = jwtConfig.extractUserId(token)
            val roles = jwtConfig.extractRoles(token)
            val userType = jwtConfig.extractUserType(token)

            Mono.just(ResponseEntity.ok(
                UserInfoResponse(
                    success = true,
                    message = "User information retrieved successfully",
                    timestamp = LocalDateTime.now(),
                    userInfo = mapOf(
                        "username" to username,
                        "userId" to userId,
                        "roles" to roles,
                        "userType" to userType
                    )
                )
            ))

        } catch (e: Exception) {
            logger.error("Error extracting user info from token", e)
            Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                UserInfoResponse(
                    success = false,
                    message = "Error extracting user information: ${e.message}",
                    timestamp = LocalDateTime.now()
                )
            ))
        }
    }

    /**
     * Test endpoint for checking gateway connectivity
     */
    @GetMapping("/ping")
    fun ping(): Mono<ResponseEntity<Map<String, Any>>> {
        val response = mapOf(
            "message" to "pong",
            "timestamp" to LocalDateTime.now(),
            "gateway" to "api-gateway",
            "status" to "operational"
        )

        return Mono.just(ResponseEntity.ok(response))
    }
}

/**
 * Data classes for responses
 */
data class TokenValidationResponse(
    val valid: Boolean,
    val message: String,
    val timestamp: LocalDateTime,
    val userInfo: Map<String, Any?> = emptyMap()
)

data class GatewayHealthResponse(
    val status: String,
    val timestamp: LocalDateTime,
    val version: String,
    val services: Map<String, String>,
    val features: Map<String, Boolean>
)

data class GatewayInfoResponse(
    val name: String,
    val version: String,
    val description: String,
    val timestamp: LocalDateTime,
    val routes: List<String>,
    val features: List<String>
)

data class UserInfoResponse(
    val success: Boolean,
    val message: String,
    val timestamp: LocalDateTime,
    val userInfo: Map<String, Any?> = emptyMap()
)