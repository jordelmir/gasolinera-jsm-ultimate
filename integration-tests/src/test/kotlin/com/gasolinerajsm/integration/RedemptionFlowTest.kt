package com.gasolinerajsm.integration

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.notNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.util.Date
import javax.sql.DataSource

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RedemptionFlowTest {

    @Autowired
    private lateinit var dataSource: DataSource

    companion object {
        private const val JWT_SECRET = "test_secret_key_for_integration_tests_123456"

        @Container
        @JvmField
        val container: DockerComposeContainer<*> = DockerComposeContainer(File("../../docker-compose.yml"))
            .withExposedService("api-gateway_1", 8080, Wait.forHttp("/actuator/health").forStatusCode(200))
            .withExposedService("auth-service_1", 8081)
            .withExposedService("redemption-service_1", 8082)
            .withExposedService("postgres-db-puntog_1", 5432)

        private var apiGatewayPort: Int = 8080

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            val postgresHost = container.getServiceHost("postgres-db-puntog_1", 5432)
            val postgresPort = container.getServicePort("postgres-db-puntog_1", 5432)
            registry.add("spring.datasource.url") { "jdbc:postgresql://$postgresHost:$postgresPort/puntog" }
            registry.add("spring.datasource.username") { "puntog" }
            registry.add("spring.datasource.password") { "password" } // Asume la pass del docker-compose

            apiGatewayPort = container.getServicePort("api-gateway_1", 8080)
        }
    }

    @BeforeAll
    fun setup() {
        RestAssured.port = apiGatewayPort
        // Aquí se podrían ejecutar migraciones o seeders si fuera necesario
    }

    @Test
    fun `should complete redemption flow successfully`() {
        val jdbcTemplate = JdbcTemplate(dataSource)
        val userId = "test-user-123"
        val qrCode = "valid-qr-code-for-testing"

        // 1. Generar un token JWT válido (simulando que auth-service lo emitió)
        val token = generateTestToken(userId)

        // 2. Llamar al redemption-service con el QR y el token
        val adUrl = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $token")
            .body("""{"qrCode": "$qrCode"}""")
            .post("/api/v1/redemptions")
        .then()
            .statusCode(200)
            .body("adUrl", notNull())
            .extract()
            .path<String>("adUrl")

        println("Received Ad URL: $adUrl")

        // 3. Simular la visualización del anuncio y llamar al endpoint de confirmación
        // En un caso real, se extraería un ID de la adUrl
        val redemptionId = "mock-redemption-id"
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $token")
            .body("""{"redemptionId": "$redemptionId"}""")
            .post("/api/v1/redemptions/confirm")
        .then()
            .statusCode(200)

        // 4. Verificar en la base de datos que los puntos y la impresión fueron registrados
        val pointsAdded: Int? = jdbcTemplate.queryForObject(
            "SELECT points FROM users WHERE id = ?",
            Int::class.java,
            userId
        )
        assert(pointsAdded != null && pointsAdded > 0)
        println("User points confirmed in DB: $pointsAdded")

        val impressionCount: Int? = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ad_impressions WHERE user_id = ?",
            Int::class.java,
            userId
        )
        assert(impressionCount == 1)
        println("Ad impression confirmed in DB: $impressionCount")
    }

    private fun generateTestToken(userId: String): String {
        val claims = mapOf("sub" to userId, "roles" to listOf("USER"))
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 600000)) // 10 minutos de validez
            .signWith(SignatureAlgorithm.HS256, JWT_SECRET.toByteArray())
            .compact()
    }
}