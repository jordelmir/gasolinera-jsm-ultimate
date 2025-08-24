package com.gasolinerajsm.sdk.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.Duration

class ApiClientConfigurationTest {

    @Test
    fun `should create default service config when service not in properties`() {
        val properties = ApiClientProperties()
        val configuration = ApiClientConfiguration(properties)

        val config = configuration.getServiceConfig("unknown-service")

        assertEquals("http://localhost:8080", config.baseUrl)
        assertEquals(Duration.ofSeconds(30), config.timeout)
        assertEquals(3, config.retries)
        assertTrue(config.enabled)
    }

    @Test
    fun `should use custom service config when service in properties`() {
        val serviceProps = mapOf(
            "auth" to ApiClientProperties.ServiceProperties(
                baseUrl = "http://custom-auth:9000",
                timeout = Duration.ofSeconds(60),
                retries = 5,
                enabled = false
            )
        )
        val properties = ApiClientProperties(services = serviceProps)
        val configuration = ApiClientConfiguration(properties)

        val config = configuration.getServiceConfig("auth")

        assertEquals("http://custom-auth:9000", config.baseUrl)
        assertEquals(Duration.ofSeconds(60), config.timeout)
        assertEquals(5, config.retries)
        assertFalse(config.enabled)
    }

    @Test
    fun `should construct baseUrl from baseUrl and port when service baseUrl is empty`() {
        val serviceProps = mapOf(
            "station" to ApiClientProperties.ServiceProperties(
                baseUrl = "",
                port = 8083
            )
        )
        val properties = ApiClientProperties(
            baseUrl = "http://localhost",
            services = serviceProps
        )
        val configuration = ApiClientConfiguration(properties)

        val config = configuration.getServiceConfig("station")

        assertEquals("http://localhost:8083", config.baseUrl)
    }

    @Test
    fun `should create http client config with correct timeouts`() {
        val properties = ApiClientProperties(
            timeout = Duration.ofSeconds(45),
            retries = 2
        )
        val configuration = ApiClientConfiguration(properties)

        val httpConfig = configuration.httpClientConfig()

        assertEquals(Duration.ofSeconds(45), httpConfig.connectTimeout)
        assertEquals(Duration.ofSeconds(45), httpConfig.readTimeout)
        assertEquals(Duration.ofSeconds(45), httpConfig.writeTimeout)
        assertEquals(2, httpConfig.retries)
    }
}

class ClientConfigurationUtilsTest {

    @Test
    fun `should create valid client config`() {
        val config = ClientConfigurationUtils.createClientConfig(
            baseUrl = "http://localhost:8080",
            timeout = Duration.ofSeconds(30),
            retries = 3,
            enabled = true
        )

        assertEquals("http://localhost:8080", config.baseUrl)
        assertEquals(Duration.ofSeconds(30), config.timeout)
        assertEquals(3, config.retries)
        assertTrue(config.enabled)
    }

    @Test
    fun `should validate config and return no errors for valid config`() {
        val config = ServiceConfig(
            baseUrl = "http://localhost:8080",
            timeout = Duration.ofSeconds(30),
            retries = 3,
            enabled = true
        )

        val errors = ClientConfigurationUtils.validateConfig(config)

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `should validate config and return errors for invalid config`() {
        val config = ServiceConfig(
            baseUrl = "",
            timeout = Duration.ofSeconds(-1),
            retries = -1,
            enabled = true
        )

        val errors = ClientConfigurationUtils.validateConfig(config)

        assertEquals(3, errors.size)
        assertTrue(errors.contains("Base URL cannot be blank"))
        assertTrue(errors.contains("Timeout must be positive"))
        assertTrue(errors.contains("Retries cannot be negative"))
    }

    @Test
    fun `should validate config and return error for invalid URL protocol`() {
        val config = ServiceConfig(
            baseUrl = "ftp://localhost:8080",
            timeout = Duration.ofSeconds(30),
            retries = 3,
            enabled = true
        )

        val errors = ClientConfigurationUtils.validateConfig(config)

        assertEquals(1, errors.size)
        assertTrue(errors.contains("Base URL must start with http:// or https://"))
    }

    @Test
    fun `should create development config with correct settings`() {
        val config = ClientConfigurationUtils.createDevelopmentConfig(8081)

        assertEquals("http://localhost:8081", config.baseUrl)
        assertEquals(Duration.ofSeconds(60), config.timeout)
        assertEquals(1, config.retries)
        assertTrue(config.enabled)
    }

    @Test
    fun `should create production config with correct settings`() {
        val config = ClientConfigurationUtils.createProductionConfig("auth")

        assertEquals("https://api.gasolinerajsm.com/auth", config.baseUrl)
        assertEquals(Duration.ofSeconds(30), config.timeout)
        assertEquals(3, config.retries)
        assertTrue(config.enabled)
    }

    @Test
    fun `should create staging config with correct settings`() {
        val config = ClientConfigurationUtils.createStagingConfig("station")

        assertEquals("https://staging-api.gasolinerajsm.com/station", config.baseUrl)
        assertEquals(Duration.ofSeconds(45), config.timeout)
        assertEquals(2, config.retries)
        assertTrue(config.enabled)
    }
}