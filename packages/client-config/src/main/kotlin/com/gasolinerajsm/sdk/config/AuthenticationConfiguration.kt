package com.gasolinerajsm.sdk.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * Authentication configuration for API clients
 */
@Configuration
class AuthenticationConfiguration(
    private val properties: ApiClientProperties
) {

    private val tokenCache = ConcurrentHashMap<String, String>()

    /**
     * Create JWT authentication filter
     */
    @Bean
    fun jwtAuthenticationFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { request ->
            val token = getAuthToken()
            if (token.isNotEmpty()) {
                val authenticatedRequest = ClientRequest.from(request)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .build()
                Mono.just(authenticatedRequest)
            } else {
                Mono.just(request)
            }
        }
    }

    /**
     * Create API key authentication filter
     */
    @Bean
    fun apiKeyAuthenticationFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { request ->
            val apiKey = getApiKey()
            if (apiKey.isNotEmpty()) {
                val authenticatedRequest = ClientRequest.from(request)
                    .header("X-API-Key", apiKey)
                    .build()
                Mono.just(authenticatedRequest)
            } else {
                Mono.just(request)
            }
        }
    }

    /**
     * Create basic authentication filter
     */
    @Bean
    fun basicAuthenticationFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { request ->
            if (properties.auth.clientId.isNotEmpty() && properties.auth.clientSecret.isNotEmpty()) {
                val credentials = "${properties.auth.clientId}:${properties.auth.clientSecret}"
                val encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.toByteArray())
                val authenticatedRequest = ClientRequest.from(request)
                    .header(HttpHeaders.AUTHORIZATION, "Basic $encodedCredentials")
                    .build()
                Mono.just(authenticatedRequest)
            } else {
                Mono.just(request)
            }
        }
    }

    /**
     * Create logging filter for debugging
     */
    @Bean
    fun loggingFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { request ->
            println("Request: ${request.method()} ${request.url()}")
            request.headers().forEach { name, values ->
                if (name != HttpHeaders.AUTHORIZATION) {
                    println("Header: $name = $values")
                }
            }
            Mono.just(request)
        }
    }

    /**
     * Create error handling filter
     */
    @Bean
    fun errorHandlingFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofResponseProcessor { response ->
            if (response.statusCode().isError) {
                println("Error response: ${response.statusCode()}")
                // Log error details but don't modify the response
            }
            Mono.just(response)
        }
    }

    /**
     * Get authentication token (placeholder implementation)
     */
    private fun getAuthToken(): String {
        // In a real implementation, this would:
        // 1. Check if we have a valid cached token
        // 2. If not, request a new token from the auth service
        // 3. Cache the token with appropriate expiration
        return tokenCache.getOrDefault("auth_token", "")
    }

    /**
     * Get API key from configuration or environment
     */
    private fun getApiKey(): String {
        return System.getenv("GASOLINERA_API_KEY") ?: ""
    }

    /**
     * Set authentication token (for use by auth service)
     */
    fun setAuthToken(token: String) {
        tokenCache["auth_token"] = token
    }

    /**
     * Clear authentication token
     */
    fun clearAuthToken() {
        tokenCache.remove("auth_token")
    }
}

/**
 * WebClient builder with authentication
 */
@Configuration
class AuthenticatedWebClientConfiguration(
    private val jwtAuthenticationFilter: ExchangeFilterFunction,
    private val loggingFilter: ExchangeFilterFunction,
    private val errorHandlingFilter: ExchangeFilterFunction
) {

    /**
     * Create authenticated WebClient builder
     */
    @Bean
    fun authenticatedWebClientBuilder(): WebClient.Builder {
        return WebClient.builder()
            .filter(jwtAuthenticationFilter)
            .filter(loggingFilter)
            .filter(errorHandlingFilter)
    }

    /**
     * Create WebClient for auth service (no JWT filter to avoid circular dependency)
     */
    @Bean
    fun authWebClient(): WebClient {
        return WebClient.builder()
            .filter(loggingFilter)
            .filter(errorHandlingFilter)
            .build()
    }
}

/**
 * Authentication helper utilities
 */
class AuthenticationHelper(
    private val authConfiguration: AuthenticationConfiguration
) {

    /**
     * Authenticate and store token
     */
    suspend fun authenticate(username: String, password: String): Boolean {
        // Placeholder implementation
        // In real implementation, this would call the auth service
        return try {
            // val authApi = AuthApi()
            // val loginRequest = LoginRequest(username, password)
            // val response = authApi.login(loginRequest)
            // authConfiguration.setAuthToken(response.token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        // Check if we have a valid token
        return false // Placeholder
    }

    /**
     * Logout and clear token
     */
    fun logout() {
        authConfiguration.clearAuthToken()
    }
}