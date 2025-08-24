package com.gasolinerajsm.apigateway.filter

import com.gasolinerajsm.apigateway.config.JwtConfig
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtConfig: JwtConfig
) : AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config>() {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private val PUBLIC_PATHS = setOf(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-otp",
            "/api/auth/refresh-token",
            "/actuator/health",
            "/actuator/info"
        )
    }

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val path = request.path.value()

            // Skip authentication for public paths
            if (isPublicPath(path)) {
                return@GatewayFilter chain.filter(exchange)
            }

            // Extract JWT token from Authorization header
            val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return@GatewayFilter handleUnauthorized(exchange, "Missing or invalid Authorization header")
            }

            val token = authHeader.substring(BEARER_PREFIX.length)

            try {
                // Validate JWT token
                if (!jwtConfig.validateToken(token)) {
                    return@GatewayFilter handleUnauthorized(exchange, "Invalid or expired JWT token")
                }

                // Extract user information from token
                val username = jwtConfig.extractUsername(token)
                val userId = jwtConfig.extractUserId(token)
                val roles = jwtConfig.extractRoles(token)
                val userType = jwtConfig.extractUserType(token)

                // Add user information to request headers for downstream services
                val modifiedRequest = request.mutate()
                    .header("X-User-Id", userId ?: "")
                    .header("X-Username", username ?: "")
                    .header("X-User-Roles", roles.joinToString(","))
                    .header("X-User-Type", userType ?: "")
                    .build()

                val modifiedExchange = exchange.mutate().request(modifiedRequest).build()

                // Check role-based access control
                if (!hasRequiredRole(path, roles)) {
                    return@GatewayFilter handleForbidden(exchange, "Insufficient permissions for this resource")
                }

                chain.filter(modifiedExchange)

            } catch (e: Exception) {
                return@GatewayFilter handleUnauthorized(exchange, "JWT token validation failed: ${e.message}")
            }
        }
    }

    private fun isPublicPath(path: String): Boolean {
        return PUBLIC_PATHS.any { publicPath ->
            path.startsWith(publicPath) || path.matches(Regex(publicPath.replace("**", ".*")))
        }
    }

    private fun hasRequiredRole(path: String, userRoles: List<String>): Boolean {
        // Define role-based access control rules
        val roleRequirements = mapOf(
            "/api/stations" to listOf("ADMIN", "OWNER", "EMPLOYEE"),
            "/api/coupons/generate" to listOf("ADMIN", "OWNER", "EMPLOYEE"),
            "/api/coupons/validate" to listOf("ADMIN", "OWNER", "EMPLOYEE"),
            "/api/ads/manage" to listOf("ADMIN", "ADVERTISER"),
            "/api/raffles/manage" to listOf("ADMIN", "OWNER"),
            "/api/redemptions/admin" to listOf("ADMIN", "OWNER")
        )

        // Check if path requires specific roles
        for ((pathPattern, requiredRoles) in roleRequirements) {
            if (path.startsWith(pathPattern)) {
                return userRoles.any { role -> requiredRoles.contains(role) }
            }
        }

        // Default: allow access for authenticated users
        return true
    }

    private fun handleUnauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.add("Content-Type", "application/json")

        val errorBody = """
            {
                "error": "Unauthorized",
                "message": "$message",
                "timestamp": "${java.time.Instant.now()}",
                "path": "${exchange.request.path.value()}"
            }
        """.trimIndent()

        val buffer = response.bufferFactory().wrap(errorBody.toByteArray())
        return response.writeWith(Mono.just(buffer))
    }

    private fun handleForbidden(exchange: ServerWebExchange, message: String): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.FORBIDDEN
        response.headers.add("Content-Type", "application/json")

        val errorBody = """
            {
                "error": "Forbidden",
                "message": "$message",
                "timestamp": "${java.time.Instant.now()}",
                "path": "${exchange.request.path.value()}"
            }
        """.trimIndent()

        val buffer = response.bufferFactory().wrap(errorBody.toByteArray())
        return response.writeWith(Mono.just(buffer))
    }

    override fun getConfigClass(): Class<Config> = Config::class.java

    class Config {
        // Configuration properties can be added here if needed
        var enabled: Boolean = true
        var skipPaths: List<String> = emptyList()
    }
}