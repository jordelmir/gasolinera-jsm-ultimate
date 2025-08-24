package com.gasolinerajsm.sdk.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Duration

/**
 * Configuration properties for API clients
 */
@ConfigurationProperties(prefix = "gasolinera.api")
data class ApiClientProperties(
    val baseUrl: String = "http://localhost",
    val timeout: Duration = Duration.ofSeconds(30),
    val retries: Int = 3,
    val auth: AuthProperties = AuthProperties(),
    val services: Map<String, ServiceProperties> = emptyMap()
) {
    data class AuthProperties(
        val enabled: Boolean = true,
        val tokenUrl: String = "/auth/token",
        val clientId: String = "",
        val clientSecret: String = ""
    )

    data class ServiceProperties(
        val baseUrl: String = "",
        val port: Int = 8080,
        val timeout: Duration = Duration.ofSeconds(30),
        val retries: Int = 3,
        val enabled: Boolean = true
    )
}

/**
 * Base configuration for all API clients
 */
@Configuration
@EnableConfigurationProperties(ApiClientProperties::class)
class ApiClientConfiguration(
    private val properties: ApiClientProperties
) {

    /**
     * Get service configuration by name
     */
    fun getServiceConfig(serviceName: String): ServiceConfig {
        val serviceProps = properties.services[serviceName]
            ?: ApiClientProperties.ServiceProperties()

        return ServiceConfig(
            baseUrl = if (serviceProps.baseUrl.isNotEmpty()) {
                serviceProps.baseUrl
            } else {
                "${properties.baseUrl}:${serviceProps.port}"
            },
            timeout = serviceProps.timeout,
            retries = serviceProps.retries,
            enabled = serviceProps.enabled
        )
    }

    /**
     * Create HTTP client configuration
     */
    @Bean
    fun httpClientConfig(): HttpClientConfig {
        return HttpClientConfig(
            connectTimeout = properties.timeout,
            readTimeout = properties.timeout,
            writeTimeout = properties.timeout,
            retries = properties.retries
        )
    }
}

/**
 * Service-specific configuration
 */
data class ServiceConfig(
    val baseUrl: String,
    val timeout: Duration,
    val retries: Int,
    val enabled: Boolean
)

/**
 * HTTP client configuration
 */
data class HttpClientConfig(
    val connectTimeout: Duration,
    val readTimeout: Duration,
    val writeTimeout: Duration,
    val retries: Int
)

/**
 * Development environment configuration
 */
@Configuration
@Profile("development", "dev", "local")
class DevelopmentApiClientConfiguration(
    private val apiClientConfiguration: ApiClientConfiguration
) {

    @Bean
    fun authServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("auth").copy(
            baseUrl = "http://localhost:8081"
        )
    }

    @Bean
    fun stationServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("station").copy(
            baseUrl = "http://localhost:8083"
        )
    }

    @Bean
    fun couponServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("coupon").copy(
            baseUrl = "http://localhost:8086"
        )
    }

    @Bean
    fun redemptionServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("redemption").copy(
            baseUrl = "http://localhost:8082"
        )
    }

    @Bean
    fun adEngineServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("adengine").copy(
            baseUrl = "http://localhost:8084"
        )
    }

    @Bean
    fun raffleServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("raffle").copy(
            baseUrl = "http://localhost:8085"
        )
    }
}

/**
 * Production environment configuration
 */
@Configuration
@Profile("production", "prod")
class ProductionApiClientConfiguration(
    private val apiClientConfiguration: ApiClientConfiguration
) {

    @Bean
    fun authServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("auth").copy(
            baseUrl = "https://api.gasolinerajsm.com/auth"
        )
    }

    @Bean
    fun stationServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("station").copy(
            baseUrl = "https://api.gasolinerajsm.com/station"
        )
    }

    @Bean
    fun couponServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("coupon").copy(
            baseUrl = "https://api.gasolinerajsm.com/coupon"
        )
    }

    @Bean
    fun redemptionServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("redemption").copy(
            baseUrl = "https://api.gasolinerajsm.com/redemption"
        )
    }

    @Bean
    fun adEngineServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("adengine").copy(
            baseUrl = "https://api.gasolinerajsm.com/adengine"
        )
    }

    @Bean
    fun raffleServiceConfig(): ServiceConfig {
        return apiClientConfiguration.getServiceConfig("raffle").copy(
            baseUrl = "https://api.gasolinerajsm.com/raffle"
        )
    }
}