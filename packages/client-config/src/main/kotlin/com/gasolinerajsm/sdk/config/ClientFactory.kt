package com.gasolinerajsm.sdk.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * Factory for creating configured API clients
 */
@Configuration
class ClientFactory(
    private val authenticatedWebClientBuilder: WebClient.Builder,
    private val authServiceConfig: ServiceConfig,
    private val stationServiceConfig: ServiceConfig,
    private val couponServiceConfig: ServiceConfig,
    private val redemptionServiceConfig: ServiceConfig,
    private val adEngineServiceConfig: ServiceConfig,
    private val raffleServiceConfig: ServiceConfig
) {

    /**
     * Create configured Auth API client
     */
    @Bean
    fun authApi(): Any { // Replace with actual AuthApi when generated
        // Placeholder - will be replaced with actual generated client
        return createClient("auth", authServiceConfig)
    }

    /**
     * Create configured Station API client
     */
    @Bean
    fun stationApi(): Any { // Replace with actual StationApi when generated
        return createClient("station", stationServiceConfig)
    }

    /**
     * Create configured Coupon API client
     */
    @Bean
    fun couponApi(): Any { // Replace with actual CouponApi when generated
        return createClient("coupon", couponServiceConfig)
    }

    /**
     * Create configured Redemption API client
     */
    @Bean
    fun redemptionApi(): Any { // Replace with actual RedemptionApi when generated
        return createClient("redemption", redemptionServiceConfig)
    }

    /**
     * Create configured Ad Engine API client
     */
    @Bean
    fun adEngineApi(): Any { // Replace with actual AdEngineApi when generated
        return createClient("adengine", adEngineServiceConfig)
    }

    /**
     * Create configured Raffle API client
     */
    @Bean
    fun raffleApi(): Any { // Replace with actual RaffleApi when generated
        return createClient("raffle", raffleServiceConfig)
    }

    /**
     * Generic client creation method
     */
    private fun createClient(serviceName: String, config: ServiceConfig): ClientWrapper {
        val webClient = authenticatedWebClientBuilder
            .baseUrl(config.baseUrl)
            .build()

        return ClientWrapper(
            serviceName = serviceName,
            baseUrl = config.baseUrl,
            webClient = webClient,
            config = config
        )
    }
}

/**
 * Wrapper for API clients with common functionality
 */
data class ClientWrapper(
    val serviceName: String,
    val baseUrl: String,
    val webClient: WebClient,
    val config: ServiceConfig
) {

    /**
     * Check if service is enabled
     */
    fun isEnabled(): Boolean = config.enabled

    /**
     * Get service timeout
     */
    fun getTimeout(): java.time.Duration = config.timeout

    /**
     * Get retry count
     */
    fun getRetries(): Int = config.retries
}

/**
 * Client configuration utilities
 */
class ClientConfigurationUtils {

    companion object {

        /**
         * Create client configuration from properties
         */
        fun createClientConfig(
            baseUrl: String,
            timeout: java.time.Duration = java.time.Duration.ofSeconds(30),
            retries: Int = 3,
            enabled: Boolean = true
        ): ServiceConfig {
            return ServiceConfig(
                baseUrl = baseUrl,
                timeout = timeout,
                retries = retries,
                enabled = enabled
            )
        }

        /**
         * Validate client configuration
         */
        fun validateConfig(config: ServiceConfig): List<String> {
            val errors = mutableListOf<String>()

            if (config.baseUrl.isBlank()) {
                errors.add("Base URL cannot be blank")
            } else if (!config.baseUrl.startsWith("http://") && !config.baseUrl.startsWith("https://")) {
                errors.add("Base URL must start with http:// or https://")
            }

            if (config.timeout.isNegative || config.timeout.isZero) {
                errors.add("Timeout must be positive")
            }

            if (config.retries < 0) {
                errors.add("Retries cannot be negative")
            }

            return errors
        }

        /**
         * Create development configuration
         */
        fun createDevelopmentConfig(port: Int): ServiceConfig {
            return createClientConfig(
                baseUrl = "http://localhost:$port",
                timeout = java.time.Duration.ofSeconds(60), // Longer timeout for dev
                retries = 1, // Fewer retries for faster feedback
                enabled = true
            )
        }

        /**
         * Create production configuration
         */
        fun createProductionConfig(serviceName: String): ServiceConfig {
            return createClientConfig(
                baseUrl = "https://api.gasolinerajsm.com/$serviceName",
                timeout = java.time.Duration.ofSeconds(30),
                retries = 3,
                enabled = true
            )
        }

        /**
         * Create staging configuration
         */
        fun createStagingConfig(serviceName: String): ServiceConfig {
            return createClientConfig(
                baseUrl = "https://staging-api.gasolinerajsm.com/$serviceName",
                timeout = java.time.Duration.ofSeconds(45),
                retries = 2,
                enabled = true
            )
        }
    }
}