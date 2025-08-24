# Client Testing Package

This package provides comprehensive testing utilities for the generated OpenAPI clients in the Gasolinera JSM monorepo.

## Features

- ✅ **Mock Client Factory**: Automatic mock creation for all API clients
- ✅ **Test Data Builders**: Pre-built test data for all API models
- ✅ **API Test Utilities**: Common testing patterns and utilities
- ✅ **Integration Test Support**: TestContainers integration for full-stack testing
- ✅ **Performance Testing**: Load testing utilities and metrics
- ✅ **Spring Test Integration**: Full Spring Boot test configuration

## Usage

### 1. Add Dependency

```kotlin
// In your service's build.gradle.kts
dependencies {
    testImplementation(project(":packages:client-testing"))
}
```

### 2. Unit Testing with Mocks

```kotlin
@SpringBootTest
@ActiveProfiles("test")
class YourServiceTest {

    @Autowired
    private lateinit var authApi: AuthApi // Automatically mocked in test profile

    @Autowired
    private lateinit var yourService: YourService

    @Test
    fun `should authenticate user successfully`() {
        // Arrange
        val phone = "+1234567890"
        val expectedToken = TestDataBuilders.createTokenResponse()

        ApiTestUtils.mockSuccessfulResponse(
            authApi,
            { verifyOtp(any()) },
            expectedToken
        )

        // Act
        val result = yourService.authenticateUser(phone, "123456")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedToken["accessToken"], result.getOrNull()?.accessToken)
    }

    @Test
    fun `should handle authentication error`() {
        // Arrange
        ApiTestUtils.mockErrorResponse(
            authApi,
            { verifyOtp(any()) },
            HttpStatus.UNAUTHORIZED,
            "Invalid OTP"
        )

        // Act & Assert
        assertThrows<WebClientResponseException> {
            yourService.authenticateUser("+1234567890", "wrong-code")
        }
    }
}
```

### 3. Integration Testing

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthServiceIntegrationTest : AuthServiceIntegrationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should complete full authentication flow`() {
        val phone = "+1234567890"

        // Test the complete flow
        val token = IntegrationTestUtils.testAuthenticationFlow(
            webTestClient,
            phone,
            "123456"
        )

        assertNotNull(token)
        assertTrue(token.isNotEmpty())
    }

    @Test
    fun `should test CRUD operations`() {
        val authToken = IntegrationTestUtils.testAuthenticationFlow(webTestClient)

        val createData = TestDataBuilders.createStationDto(
            name = "Integration Test Station",
            address = "123 Integration St"
        )

        val updateData = createData.toMutableMap().apply {
            put("name", "Updated Station")
        }

        val entityId = IntegrationTestUtils.testCrudOperations(
            webTestClient,
            "/api/stations",
            createData,
            updateData,
            authToken
        )

        assertTrue(entityId > 0)
    }
}
```

### 4. Performance Testing

```kotlin
@Test
fun `should handle concurrent requests`() {
    val result = PerformanceTestUtils.runLoadTest(
        testName = "Auth Service Load Test",
        concurrency = 10,
        iterations = 50
    ) {
        val response = authApi.requestOtp(
            TestDataBuilders.createOtpRequest("+1234567890")
        )
        assertNotNull(response)
    }

    result.printSummary()

    // Assertions
    assertTrue(result.successfulRequests > 0)
    assertTrue(result.averageTimeMs < 1000) // Should be under 1 second
    assertTrue(result.requestsPerSecond > 10) // Should handle at least 10 RPS
}
```

### 5. TestContainers Integration

```kotlin
// Extend base integration test class
class MyServiceIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `should work with real database`() {
        // PostgreSQL and Redis containers are automatically started
        // Database is automatically configured

        // Your test code here
        val stations = stationRepository.findAll()
        assertTrue(stations.isEmpty()) // Fresh database for each test
    }
}

// Or use service-specific base classes
class AuthFlowIntegrationTest : AuthServiceIntegrationTest() {

    @Test
    fun `should authenticate against real auth service`() {
        // Auth service container is automatically started
        // Service URL is automatically configured

        val token = IntegrationTestUtils.testAuthenticationFlow(webTestClient)
        assertNotNull(token)
    }
}
```

## Mock Client Factory

The `MockClientFactory` automatically provides mocked versions of all API clients when running in test profile:

```kotlin
@Configuration
@Profile("test")
class MockClientFactory {

    @Bean
    @Primary
    fun mockAuthApi(): AuthApi = mockk<AuthApi>(relaxed = true)

    @Bean
    @Primary
    fun mockStationApi(): StationApi = mockk<StationApi>(relaxed = true)

    // ... other clients
}
```

All mocks are created with `relaxed = true` by default, meaning they return sensible default values for all method calls.

## Test Data Builders

Pre-built test data for all API models:

```kotlin
// Authentication
val otpRequest = TestDataBuilders.createOtpRequest("+1234567890")
val otpVerifyRequest = TestDataBuilders.createOtpVerifyRequest("+1234567890", "123456")
val tokenResponse = TestDataBuilders.createTokenResponse("access-token", "refresh-token")

// Stations
val stationDto = TestDataBuilders.createStationDto(
    id = 1L,
    name = "Test Station",
    address = "123 Test St",
    latitude = 9.9281,
    longitude = -84.0907
)

// Coupons
val couponDto = TestDataBuilders.createCouponDto(
    id = 1L,
    qrCode = "TEST-QR-CODE",
    amount = 5000.0,
    stationId = 1L
)

// Redemptions
val redemptionDto = TestDataBuilders.createRedemptionDto(
    id = 1L,
    couponId = 1L,
    userId = 1L,
    points = 1
)

// Ads
val adDto = TestDataBuilders.createAdDto(
    id = 1L,
    title = "Test Ad",
    content = "Test ad content",
    duration = 30
)

// Raffles
val raffleDto = TestDataBuilders.createRaffleDto(
    id = 1L,
    name = "Weekly Raffle",
    prize = "₡40,000"
)
```

## API Test Utilities

Common testing patterns:

```kotlin
// JSON conversion
val json = ApiTestUtils.toJson(testObject)
val object = ApiTestUtils.fromJson<MyClass>(json)

// Mock responses
ApiTestUtils.mockSuccessfulResponse(client, { method() }, response)
ApiTestUtils.mockErrorResponse(client, { method() }, HttpStatus.BAD_REQUEST)

// Async mocking
ApiTestUtils.mockSuccessfulAsyncResponse(client, { suspendMethod() }, response)
ApiTestUtils.mockAsyncErrorResponse(client, { suspendMethod() }, HttpStatus.INTERNAL_SERVER_ERROR)

// Response validation
ApiTestUtils.assertResponseStructure(response, listOf("id", "name", "active"))

// Headers
val headers = ApiTestUtils.createTestHeaders()
val authHeaders = ApiTestUtils.createAuthenticatedHeaders("token")

// Async testing
val result = ApiTestUtils.runAsyncTest {
    // Your async test code
}
```

## TestContainers Integration

Automatic container management for integration tests:

### Available Containers

- **PostgreSQL**: Shared database container for all tests
- **Redis**: Shared cache container for all tests
- **RabbitMQ**: Message queue container (when needed)
- **Service Containers**: Individual service containers for integration testing

### Base Classes

- `BaseIntegrationTest`: PostgreSQL + Redis containers
- `AuthServiceIntegrationTest`: Includes Auth service container
- `StationServiceIntegrationTest`: Includes Station service container

### Container Utilities

```kotlin
// Wait for container health
ContainerTestUtils.waitForContainerHealth(container)

// Get container URL
val baseUrl = ContainerTestUtils.getContainerBaseUrl(container)

// Execute SQL
ContainerTestUtils.executeSQL(postgresContainer, "INSERT INTO ...")

// Clean database
ContainerTestUtils.cleanDatabase(postgresContainer)
```

## Performance Testing

Load testing utilities with detailed metrics:

```kotlin
val result = PerformanceTestUtils.runLoadTest(
    testName = "API Load Test",
    concurrency = 10,    // 10 concurrent threads
    iterations = 100     // 100 requests per thread
) {
    // Your test code here
    apiClient.someMethod()
}

// Print detailed results
result.printSummary()

// Access metrics
println("Success rate: ${result.successfulRequests.toDouble() / result.totalRequests * 100}%")
println("Average response time: ${result.averageTimeMs}ms")
println("Requests per second: ${result.requestsPerSecond}")
```

### Load Test Results

The `LoadTestResult` class provides comprehensive metrics:

- Total/successful/failed requests
- Response time statistics (min/max/average)
- Requests per second
- Error breakdown by type
- Success rate percentage

## Configuration

### Test Profile Configuration

```yaml
# application-test.yml
spring:
  profiles:
    active: test

gasolinera:
  api:
    services:
      auth:
        enabled: false # Use mocks by default
      station:
        enabled: false
      # ... other services
```

### TestContainers Configuration

```kotlin
// Automatic configuration via @DynamicPropertySource
@DynamicPropertySource
fun configureProperties(registry: DynamicPropertyRegistry) {
    registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
    registry.add("spring.datasource.username") { postgresContainer.username }
    registry.add("spring.datasource.password") { postgresContainer.password }
    // ... other properties
}
```

## Best Practices

### 1. Test Organization

```kotlin
// Unit tests: Use mocks
@SpringBootTest
@ActiveProfiles("test")
class UnitTest {
    // Fast, isolated tests with mocks
}

// Integration tests: Use containers
class IntegrationTest : BaseIntegrationTest() {
    // Slower, full-stack tests with real services
}

// Performance tests: Separate test class
class PerformanceTest {
    // Load and stress tests
}
```

### 2. Test Data Management

```kotlin
// Use builders for consistent test data
val station = TestDataBuilders.createStationDto()
    .toMutableMap()
    .apply { put("name", "Custom Name") }

// Clean up after tests
@AfterEach
fun cleanup() {
    ContainerTestUtils.cleanDatabase(postgresContainer)
}
```

### 3. Mock Configuration

```kotlin
// Configure mocks in setup
@BeforeEach
fun setup() {
    ApiTestUtils.mockSuccessfulResponse(
        authApi,
        { requestOtp(any()) },
        TestDataBuilders.createTokenResponse()
    )
}
```

### 4. Error Testing

```kotlin
// Test all error scenarios
@Test
fun `should handle various error responses`() {
    listOf(
        HttpStatus.BAD_REQUEST to "Invalid request",
        HttpStatus.UNAUTHORIZED to "Invalid credentials",
        HttpStatus.INTERNAL_SERVER_ERROR to "Server error"
    ).forEach { (status, message) ->
        ApiTestUtils.mockErrorResponse(client, { method() }, status, message)

        val exception = assertThrows<WebClientResponseException> {
            service.callMethod()
        }

        assertEquals(status.value(), exception.statusCode.value())
    }
}
```

## Troubleshooting

### Common Issues

1. **Containers not starting**: Check Docker is running and ports are available
2. **Tests timing out**: Increase timeout values or check container health
3. **Mock not working**: Ensure `@Profile("test")` is active
4. **Database conflicts**: Use `@Transactional` or clean database between tests

### Debug Configuration

```yaml
# Enable debug logging for tests
logging:
  level:
    com.gasolinerajsm.sdk.testing: DEBUG
    org.testcontainers: DEBUG
    org.springframework.test: DEBUG
```

---

_This package is part of the Gasolinera JSM OpenAPI client generation system._
