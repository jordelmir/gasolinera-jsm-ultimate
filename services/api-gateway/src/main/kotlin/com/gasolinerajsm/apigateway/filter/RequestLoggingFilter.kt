package com.gasolinerajsm.apigateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant

@Component
class RequestLoggingFilter : AbstractGatewayFilterFactory<RequestLoggingFilter.Config>() {

    private val logger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val startTime = Instant.now()
            val request = exchange.request
            val requestId = exchange.request.headers.getFirst("X-Gateway-Request-Id") ?: "unknown"

            // Log incoming request
            if (config.logRequests) {
                logger.info(
                    "Incoming Request - ID: {}, Method: {}, Path: {}, Remote: {}, User-Agent: {}",
                    requestId,
                    request.method,
                    request.path.value(),
                    getClientIp(exchange),
                    request.headers.getFirst("User-Agent") ?: "unknown"
                )

                // Log headers if enabled (be careful with sensitive data)
                if (config.logHeaders) {
                    val safeHeaders = request.headers.toSingleValueMap()
                        .filterKeys { !it.equals("authorization", ignoreCase = true) }
                    logger.debug("Request Headers - ID: {}, Headers: {}", requestId, safeHeaders)
                }
            }

            // Continue with the request and log response
            chain.filter(exchange).doFinally { signalType ->
                val endTime = Instant.now()
                val duration = java.time.Duration.between(startTime, endTime).toMillis()
                val response = exchange.response

                if (config.logResponses) {
                    logger.info(
                        "Outgoing Response - ID: {}, Status: {}, Duration: {}ms, Signal: {}",
                        requestId,
                        response.statusCode?.value() ?: "unknown",
                        duration,
                        signalType
                    )
                }

                // Log slow requests
                if (duration > config.slowRequestThreshold) {
                    logger.warn(
                        "Slow Request Detected - ID: {}, Path: {}, Duration: {}ms",
                        requestId,
                        request.path.value(),
                        duration
                    )
                }
            }
        }
    }

    private fun getClientIp(exchange: ServerWebExchange): String {
        val request = exchange.request

        // Check for X-Forwarded-For header
        val xForwardedFor = request.headers.getFirst("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }

        // Check for X-Real-IP header
        val xRealIp = request.headers.getFirst("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        // Fallback to remote address
        return request.remoteAddress?.address?.hostAddress ?: "unknown"
    }

    override fun getConfigClass(): Class<Config> = Config::class.java

    class Config {
        var logRequests: Boolean = true
        var logResponses: Boolean = true
        var logHeaders: Boolean = false
        var slowRequestThreshold: Long = 1000 // milliseconds
    }
}