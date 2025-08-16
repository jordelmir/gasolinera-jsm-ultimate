package com.gasolinerajsm.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.Base64
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedemptionFlowTest {

    private val network = Network.newNetwork()
    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    // Testcontainers
    private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
        .withDatabaseName("puntog")
        .withUsername("puntog")
        .withPassword("changeme")
        .withNetwork(network)
        .withNetworkAliases("postgres")
        .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\n", 2))

    private val redis = GenericContainer(DockerImageName.parse("redis:7-alpine"))
        .withNetwork(network)
        .withNetworkAliases("redis")
        .withExposedPorts(6379)
        .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\n", 1))

    private val zookeeper = GenericContainer(DockerImageName.parse("confluentinc/cp-zookeeper:7.3.0"))
        .withNetwork(network)
        .withNetworkAliases("zookeeper")
        .withExposedPorts(2181)
        .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
        .withEnv("ZOOKEEPER_TICK_TIME", "2000")
        .waitingFor(Wait.forLogMessage(".*ZooKeeper audit is disabled.*\n", 1))

    private val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
        .withNetwork(network)
        .withNetworkAliases("kafka")
        .withExposedPorts(9092)
        .withEnv("KAFKA_BROKER_ID", "1")
        .withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
        .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092")
        .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
        .dependsOn(zookeeper)
        .waitingFor(Wait.forLogMessage(".*Kafka Server started.*\n", 1))

    private val debezium = GenericContainer(DockerImageName.parse("debezium/connect:2.5"))
        .withNetwork(network)
        .withNetworkAliases("debezium")
        .withExposedPorts(8083)
        .withEnv("BOOTSTRAP_SERVERS", "kafka:9092")
        .withEnv("GROUP_ID", "1")
        .withEnv("CONFIG_STORAGE_TOPIC", "connect_configs")
        .withEnv("OFFSET_STORAGE_TOPIC", "connect_offsets")
        .withEnv("STATUS_STORAGE_TOPIC", "connect_statuses")
        .dependsOn(kafka, postgres)
        .waitingFor(Wait.forLogMessage(".*Kafka Connect started.*\n", 1))

    // Microservices
    private val authService = GenericContainer(DockerImageName.parse("gasolinera-jsm-ultimate-auth-service")) // Assuming image name
        .withNetwork(network)
        .withNetworkAliases("auth-service")
        .withExposedPorts(8080)
        .withEnv("SPRING_PROFILES_ACTIVE", "docker")
        .withEnv("POSTGRES_HOST", "postgres")
        .withEnv("REDIS_HOST", "redis")
        .withEnv("JWT_SECRET", "${generateRandomSecret()}") // Generate a random secret for testing
        .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).forStatusCode(200))
        .dependsOn(postgres, redis)

    private val redemptionService = GenericContainer(DockerImageName.parse("gasolinera-jsm-ultimate-redemption-service")) // Assuming image name
        .withNetwork(network)
        .withNetworkAliases("redemption-service")
        .withExposedPorts(8080)
        .withEnv("SPRING_PROFILES_ACTIVE", "docker")
        .withEnv("POSTGRES_HOST", "postgres")
        .withEnv("REDIS_HOST", "redis")
        .withEnv("RABBITMQ_HOST", "rabbitmq")
        .withEnv("QR_PUBLIC_KEY", "${generateRandomEcdsaPublicKey()}") // Generate a random public key for testing
        .withEnv("AD_ENGINE_URL", "http://ad-engine:8080")
        .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).forStatusCode(200))
        .dependsOn(postgres, redis, kafka, authService) // Depends on authService for JWT validation

    private val adEngine = GenericContainer(DockerImageName.parse("gasolinera-jsm-ultimate-ad-engine")) // Assuming image name
        .withNetwork(network)
        .withNetworkAliases("ad-engine")
        .withExposedPorts(8080)
        .withEnv("SPRING_PROFILES_ACTIVE", "docker")
        .withEnv("POSTGRES_HOST", "postgres")
        .withEnv("AD_FALLBACK_URL", "http://example.com/fallback-ad.mp4")
        .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).forStatusCode(200))
        .dependsOn(postgres)

    @BeforeAll
    fun setup() {
        // Start all containers
        postgres.start()
        redis.start()
        zookeeper.start()
        kafka.start()
        debezium.start()
        authService.start()
        adEngine.start()
        redemptionService.start()

        // Wait for all services to be fully up and running
        Thread.sleep(Duration.ofSeconds(10).toMillis()) // Give services some time to initialize
    }

    @AfterAll
    fun cleanup() {
        // Stop all containers
        redemptionService.stop()
        adEngine.stop()
        authService.stop()
        debezium.stop()
        kafka.stop()
        zookeeper.stop()
        redis.stop()
        postgres.stop()
        network.close()
    }

    @Test
    fun `full redemption flow should work`() {
        val userPhone = "+1234567890"
        val stationId = 123L
        val dispenserId = 456L

        // 1. Request OTP from auth-service
        val authServicePort = authService.getMappedPort(8080)
        val otpRequest = mapOf("phone" to userPhone)
        val otpResponse = restTemplate.postForEntity("http://localhost:$authServicePort/auth/otp/request", otpRequest, Void::class.java)
        assertEquals(HttpStatus.OK, otpResponse.statusCode)

        // In a real scenario, we'd get the OTP from a messaging service. For testing, we assume it's logged.
        // For this test, we'll use a mock OTP or retrieve it from logs if possible.
        val otpCode = "123456" // Mock OTP for testing

        // 2. Verify OTP and get JWT token
        val otpVerifyRequest = mapOf("phone" to userPhone, "code" to otpCode)
        val tokenResponse = restTemplate.postForEntity("http://localhost:$authServicePort/auth/otp/verify", otpVerifyRequest, Map::class.java)
        assertEquals(HttpStatus.OK, tokenResponse.statusCode)
        val accessToken = tokenResponse.body?.get("accessToken") as String
        assertNotNull(accessToken)

        // 3. Generate a valid QR token (ECDSA signed)
        val qrPayload = mapOf(
            "s" to stationId.toString(),
            "d" to dispenserId.toString(),
            "n" to UUID.randomUUID().toString(),
            "t" to (System.currentTimeMillis() / 1000),
            "exp" to (System.currentTimeMillis() / 1000) + 3600 // Expires in 1 hour
        )
        val privateKeyPem = generateRandomEcdsaPrivateKey() // Use the generated private key
        val signedQrToken = signQrPayload(qrPayload, privateKeyPem)

        // 4. Call redemption-service to initiate redemption
        val redemptionServicePort = redemptionService.getMappedPort(8080)
        val redeemRequest = mapOf("qrToken" to signedQrToken, "userId" to userPhone) // userId is passed in command
        val redeemResponse = restTemplate.postForEntity("http://localhost:$redemptionServicePort/redeem", redeemRequest, Map::class.java)
        assertEquals(HttpStatus.OK, redeemResponse.statusCode)
        val adUrl = redeemResponse.body?.get("adUrl") as String
        val redemptionId = redeemResponse.body?.get("redemptionId") as String
        val campaignId = (redeemResponse.body?.get("campaignId") as Number).toLong()
        assertNotNull(adUrl)
        assertNotNull(redemptionId)
        assertTrue(adUrl.startsWith("http"))

        // 5. Simulate ad confirmation
        val confirmAdRequest = mapOf("sessionId" to redemptionId)
        val confirmAdResponse = restTemplate.postForEntity("http://localhost:$redemptionServicePort/redeem/confirm-ad", confirmAdRequest, Map::class.java)
        assertEquals(HttpStatus.OK, confirmAdResponse.statusCode)
        val balance = (confirmAdResponse.body?.get("balance") as Number).toInt()
        assertTrue(balance > 0)

        // 6. Verify database entries (ad_impressions and user points - assuming a user_points table exists)
        // This part would require direct JDBC access to the postgres container
        // For simplicity, we'll just check the ad_impressions table for now.
        val adEnginePort = adEngine.getMappedPort(8080)
        val impressions = restTemplate.getForEntity("http://localhost:$adEnginePort/ad/impressions?campaignId=$campaignId", List::class.java) // Assuming an endpoint to list impressions
        assertEquals(HttpStatus.OK, impressions.statusCode)
        // Further assertions on impressions content would go here
    }

    // Helper functions for JWT and ECDSA signing
    private fun generateRandomSecret(): String {
        return Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS256).encoded)
    }

    private fun generateRandomEcdsaPrivateKey(): String {
        val keyPair = java.security.KeyPairGenerator.getInstance("EC").apply { initialize(256) }.generateKeyPair()
        return Base64.getEncoder().encodeToString(keyPair.private.encoded)
    }

    private fun generateRandomEcdsaPublicKey(): String {
        val keyPair = java.security.KeyPairGenerator.getInstance("EC").apply { initialize(256) }.generateKeyPair()
        return Base64.getEncoder().encodeToString(keyPair.public.encoded)
    }

    private fun signQrPayload(payload: Map<String, Any>, privateKeyPem: String): String {
        val payloadBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(payload))
        val privateKeyBytes = Base64.getDecoder().decode(privateKeyPem)
        val privateKeySpec = java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes)
        val keyFactory = java.security.KeyFactory.getInstance("EC")
        val privateKey = keyFactory.generatePrivate(privateKeySpec)

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(payloadBase64.toByteArray(Charsets.UTF_8))
        val signedBytes = signature.sign()

        return "$payloadBase64.${Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes)}"
    }
}
