package com.gasolinerajsm.apigateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import reactor.core.publisher.Mono

@Configuration
class RateLimitingConfig {

    @Value("\${rate-limiting.default.requests-per-second:100}")
    private var defaultRequestsPerSecond: Int = 100

    @Value("\${rate-limiting.default.burst-capacity:200}")
    private var defaultBurstCapacity: Int = 200

    @Bean
    @Primary
    fun redisRateLimiter(): RedisRateLimiter {
        return RedisRateLimiter(defaultRequestsPerSecond, defaultBurstCapacity, 1)
    }

    @Bean
    fun authRateLimiter(): RedisRateLimiter {
        // More restrictive rate limiting for auth endpoints
        return RedisRateLimiter(5, 10, 1)
    }

    @Bean
    fun couponRateLimiter(): RedisRateLimiter {
        // Moderate rate limiting for coupon generation
        return RedisRateLimiter(20, 40, 1)
    }

    @Bean
    @Primary
    fun userKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            // Try to get user ID from JWT token first
            val userId = exchange.request.headers.getFirst("X-User-Id")
            if (!userId.isNullOrBlank()) {
                return@KeyResolver Mono.just("user:$userId")
            }

            // Fallback to IP address
            val clientIp = getClientIp(exchange)
            Mono.just("ip:$clientIp")
        }
    }

    @Bean
    fun ipKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            val clientIp = getClientIp(exchange)
            Mono.just("ip:$clientIp")
        }
    }

    @Bean
    fun pathKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            val path = exchange.request.path.value()
            val clientIp = getClientIp(exchange)
            Mono.just("path:$path:ip:$clientIp")
        }
    }

    private fun getClientIp(exchange: org.springframework.web.server.ServerWebExchange): String {
        val request = exchange.request

        // Check for X-Forwarded-For header (common in load balancers)
        val xForwardedFor = request.headers.getFirst("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }

        // Check for X-Real-IP header (common in nginx)
        val xRealIp = request.headers.getFirst("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        // Check for CF-Connecting-IP header (Cloudflare)
        val cfConnectingIp = request.headers.getFirst("CF-Connecting-IP")
        if (!cfConnectingIp.isNullOrBlank()) {
            return cfConnectingIp
        }

        // Fallback to remote address
        return request.remoteAddress?.address?.hostAddress ?: "unknown"
    }
}