package com.gasolinerajsm.apigateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component

@Component
class ResponseLoggingFilter : AbstractGatewayFilterFactory<ResponseLoggingFilter.Config>() {

    private val logger = LoggerFactory.getLogger(ResponseLoggingFilter::class.java)

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val requestId = request.headers.getFirst("X-Gateway-Request-Id") ?: "unknown"

            chain.filter(exchange).doOnSuccess {
                val response = exchange.response

                if (config.logResponses) {
                    logger.info(
                        "Response - ID: {}, Status: {}, Headers: {}",
                        requestId,
                        response.statusCode?.value() ?: "unknown",
                        if (config.logHeaders) response.headers.toSingleValueMap() else "hidden"
                    )
                }

                // Log error responses with more detail
                val statusCode = response.statusCode?.value() ?: 0
                if (statusCode >= 400) {
                    logger.warn(
                        "Error Response - ID: {}, Path: {}, Status: {}, Method: {}",
                        requestId,
                        request.path.value(),
                        statusCode,
                        request.method
                    )
                }
            }.doOnError { error ->
                logger.error(
                    "Request Failed - ID: {}, Path: {}, Error: {}",
                    requestId,
                    request.path.value(),
                    error.message,
                    error
                )
            }
        }
    }

    override fun getConfigClass(): Class<Config> = Config::class.java

    class Config {
        var logResponses: Boolean = true
        var logHeaders: Boolean = false
    }
}