package com.gasolinerajsm.sdk.testing

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

/**
 * Test container utilities for integration testing
 */
class TestContainerUtils {

    companion object {

        /**
         * Create PostgreSQL test container
         */
        fun createPostgreSQLContainer(): PostgreSQLContainer<*> {
            return PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("gasolinera_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true)
        }

        /**
         * Create Redis test container
         */
        fun createRedisContainer(): GenericContainer<*> {
            return GenericContainer(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379)
                .withReuse(true)
                .waitingFor(Wait.forListeningPort())
        }

        /**
         * Create RabbitMQ test container
         */
        fun createRabbitMQContainer(): GenericContainer<*> {
            return GenericContainer(DockerImageName.parse("rabbitmq:3-management-alpine"))
                .withExposedPorts(5672, 15672)
                .withEnv("RABBITMQ_DEFAULT_USER", "test")
                .withEnv("RABBITMQ_DEFAULT_PASS", "test")
                .withReuse(true)
                .waitingFor(Wait.forListeningPort())
        }

        /**
         * Create service container for testing
         */
        fun createServiceContainer(
            imageName: String,
            port: Int,
            environmentVariables: Map<String, String> = emptyMap()
        ): GenericContainer<*> {
            val container = GenericContainer(DockerImageName.parse(imageName))
                .withExposedPorts(port)
                .waitingFor(Wait.forHttp("/actuator/health").forPort(port))

            environmentVariables.forEach { (key, value) ->
                container.withEnv(key, value)
            }

            return container
        }
    }
}

/**
 * Base class for integration tests with containers
 */
abstract class BaseIntegrationTest {

    companion object {

        // Shared containers for all tests
        val postgresContainer: PostgreSQLContainer<*> = TestContainerUtils.createPostgreSQLContainer()
        val redisContainer: GenericContainer<*> = TestContainerUtils.createRedisContainer()

        init {
            // Start containers once for all tests
            postgresContainer.start()
            redisContainer.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // Database properties
            registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgresContainer.username }
            registry.add("spring.datasource.password") { postgresContainer.password }

            // Redis properties
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }

            // Test profile
            registry.add("spring.profiles.active") { "test" }

            // Disable external services in tests
            registry.add("gasolinera.api.services.auth.enabled") { "false" }
            registry.add("gasolinera.api.services.station.enabled") { "false" }
            registry.add("gasolinera.api.services.coupon.enabled") { "false" }
            registry.add("gasolinera.api.services.redemption.enabled") { "false" }
            registry.add("gasolinera.api.services.adengine.enabled") { "false" }
            registry.add("gasolinera.api.services.raffle.enabled") { "false" }
        }
    }
}

/**
 * Service-specific integration test base classes
 */
abstract class AuthServiceIntegrationTest : BaseIntegrationTest() {

    companion object {
        val authServiceContainer: GenericContainer<*> = TestContainerUtils.createServiceContainer(
            "gasolinera/auth-service:test",
            8081,
            mapOf(
                "SPRING_PROFILES_ACTIVE" to "test",
                "SPRING_DATASOURCE_URL" to postgresContainer.jdbcUrl,
                "SPRING_DATASOURCE_USERNAME" to postgresContainer.username,
                "SPRING_DATASOURCE_PASSWORD" to postgresContainer.password
            )
        )

        init {
            authServiceContainer.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureAuthServiceProperties(registry: DynamicPropertyRegistry) {
            registry.add("gasolinera.api.services.auth.baseUrl") {
                "http://${authServiceContainer.host}:${authServiceContainer.getMappedPort(8081)}"
            }
            registry.add("gasolinera.api.services.auth.enabled") { "true" }
        }
    }
}

abstract class StationServiceIntegrationTest : BaseIntegrationTest() {

    companion object {
        val stationServiceContainer: GenericContainer<*> = TestContainerUtils.createServiceContainer(
            "gasolinera/station-service:test",
            8083,
            mapOf(
                "SPRING_PROFILES_ACTIVE" to "test",
                "SPRING_DATASOURCE_URL" to postgresContainer.jdbcUrl,
                "SPRING_DATASOURCE_USERNAME" to postgresContainer.username,
                "SPRING_DATASOURCE_PASSWORD" to postgresContainer.password
            )
        )

        init {
            stationServiceContainer.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureStationServiceProperties(registry: DynamicPropertyRegistry) {
            registry.add("gasolinera.api.services.station.baseUrl") {
                "http://${stationServiceContainer.host}:${stationServiceContainer.getMappedPort(8083)}"
            }
            registry.add("gasolinera.api.services.station.enabled") { "true" }
        }
    }
}

/**
 * Test configuration for containers
 */
@TestConfiguration
class TestContainerConfiguration {

    /**
     * Override database configuration for tests
     */
    @Bean
    @Primary
    fun testDataSourceProperties(): Map<String, String> {
        return mapOf(
            "spring.datasource.url" to BaseIntegrationTest.postgresContainer.jdbcUrl,
            "spring.datasource.username" to BaseIntegrationTest.postgresContainer.username,
            "spring.datasource.password" to BaseIntegrationTest.postgresContainer.password,
            "spring.jpa.hibernate.ddl-auto" to "create-drop",
            "spring.jpa.show-sql" to "true"
        )
    }

    /**
     * Override Redis configuration for tests
     */
    @Bean
    @Primary
    fun testRedisProperties(): Map<String, String> {
        return mapOf(
            "spring.data.redis.host" to BaseIntegrationTest.redisContainer.host,
            "spring.data.redis.port" to BaseIntegrationTest.redisContainer.getMappedPort(6379).toString()
        )
    }
}

/**
 * Utilities for working with test containers
 */
class ContainerTestUtils {

    companion object {

        /**
         * Wait for container to be healthy
         */
        fun waitForContainerHealth(
            container: GenericContainer<*>,
            healthEndpoint: String = "/actuator/health",
            maxAttempts: Int = 30,
            delayMs: Long = 1000
        ): Boolean {
            val baseUrl = "http://${container.host}:${container.firstMappedPort}"
            val webTestClient = ApiTestUtils.createWebTestClient(baseUrl)

            return IntegrationTestUtils.waitForService(
                webTestClient,
                healthEndpoint,
                maxAttempts,
                delayMs
            )
        }

        /**
         * Get container base URL
         */
        fun getContainerBaseUrl(container: GenericContainer<*>): String {
            return "http://${container.host}:${container.firstMappedPort}"
        }

        /**
         * Execute SQL in PostgreSQL container
         */
        fun executeSQL(container: PostgreSQLContainer<*>, sql: String): Boolean {
            return try {
                container.createConnection("").use { connection ->
                    connection.createStatement().use { statement ->
                        statement.execute(sql)
                    }
                }
                true
            } catch (e: Exception) {
                println("Failed to execute SQL: $sql")
                e.printStackTrace()
                false
            }
        }

        /**
         * Clean database for tests
         */
        fun cleanDatabase(container: PostgreSQLContainer<*>) {
            val cleanupSQL = """
                TRUNCATE TABLE stations CASCADE;
                TRUNCATE TABLE coupons CASCADE;
                TRUNCATE TABLE redemptions CASCADE;
                TRUNCATE TABLE users CASCADE;
                TRUNCATE TABLE ads CASCADE;
                TRUNCATE TABLE raffles CASCADE;
            """.trimIndent()

            executeSQL(container, cleanupSQL)
        }
    }
}