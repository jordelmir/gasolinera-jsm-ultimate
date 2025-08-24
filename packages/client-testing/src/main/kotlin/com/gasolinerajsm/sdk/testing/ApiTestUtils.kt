package com.gasolinerajsm.sdk.testing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

/**
 * Utilities for API testing
 */
class ApiTestUtils {

    companion object {

        val objectMapper: ObjectMapper = jacksonObjectMapper()

        /**
         * Create WebTestClient with common configuration
         */
        fun createWebTestClient(baseUrl: String): WebTestClient {
            return WebTestClient.bindToServer()
                .baseUrl(baseUrl)
                .responseTimeout(Duration.ofSeconds(30))
                .build()
        }

        /**
         * Convert object to JSON string
         */
        fun toJson(obj: Any): String {
            return objectMapper.writeValueAsString(obj)
        }

        /**
         * Convert JSON string to object
         */
        inline fun <reified T> fromJson(json: String): T {
            return objectMapper.readValue(json, T::class.java)
        }

        /**
         * Create WebClientResponseException for testing error scenarios
         */
        fun createWebClientException(
            status: HttpStatus,
            message: String = "Test error",
            responseBody: String = ""
        ): WebClientResponseException {
            return WebClientResponseException.create(
                status.value(),
                message,
                HttpHeaders.EMPTY,
                responseBody.toByteArray(),
                null
            )
        }

        /**
         * Mock successful API response
         */
        inline fun <reified T, reified R> mockSuccessfulResponse(
            client: T,
            crossinline methodCall: T.() -> R,
            response: R
        ) {
            every { client.methodCall() } returns response
        }

        /**
         * Mock successful async API response
         */
        inline fun <reified T, reified R> mockSuccessfulAsyncResponse(
            client: T,
            noinline methodCall: suspend T.() -> R,
            response: R
        ) {
            coEvery { client.methodCall() } returns response
        }

        /**
         * Mock API error response
         */
        inline fun <reified T, reified R> mockErrorResponse(
            client: T,
            crossinline methodCall: T.() -> R,
            status: HttpStatus,
            message: String = "Test error"
        ) {
            every { client.methodCall() } throws createWebClientException(status, message)
        }

        /**
         * Mock async API error response
         */
        inline fun <reified T, reified R> mockAsyncErrorResponse(
            client: T,
            noinline methodCall: suspend T.() -> R,
            status: HttpStatus,
            message: String = "Test error"
        ) {
            coEvery { client.methodCall() } throws createWebClientException(status, message)
        }

        /**
         * Execute async test
         */
        fun <T> runAsyncTest(block: suspend () -> T): T {
            return runBlocking { block() }
        }

        /**
         * Assert API response structure
         */
        fun assertResponseStructure(response: Any, expectedFields: List<String>) {
            val responseMap = when (response) {
                is Map<*, *> -> response as Map<String, Any>
                else -> objectMapper.convertValue(response, Map::class.java) as Map<String, Any>
            }

            expectedFields.forEach { field ->
                assert(responseMap.containsKey(field)) {
                    "Response missing required field: $field. Available fields: ${responseMap.keys}"
                }
            }
        }

        /**
         * Create test HTTP headers
         */
        fun createTestHeaders(): Map<String, String> {
            return mapOf(
                "Content-Type" to MediaType.APPLICATION_JSON_VALUE,
                "Accept" to MediaType.APPLICATION_JSON_VALUE,
                "X-Test-Client" to "true"
            )
        }

        /**
         * Create authenticated test headers
         */
        fun createAuthenticatedHeaders(token: String): Map<String, String> {
            return createTestHeaders() + mapOf(
                "Authorization" to "Bearer $token"
            )
        }
    }
}

/**
 * Integration test utilities
 */
class IntegrationTestUtils {

    companion object {

        /**
         * Wait for service to be ready
         */
        fun waitForService(
            webTestClient: WebTestClient,
            healthEndpoint: String = "/actuator/health",
            maxAttempts: Int = 30,
            delayMs: Long = 1000
        ): Boolean {
            repeat(maxAttempts) { attempt ->
                try {
                    webTestClient.get()
                        .uri(healthEndpoint)
                        .exchange()
                        .expectStatus().isOk
                    return true
                } catch (e: Exception) {
                    if (attempt == maxAttempts - 1) {
                        throw e
                    }
                    Thread.sleep(delayMs)
                }
            }
            return false
        }

        /**
         * Test authentication flow
         */
        fun testAuthenticationFlow(
            webTestClient: WebTestClient,
            phone: String = "+1234567890",
            otpCode: String = "123456"
        ): String {
            // Request OTP
            webTestClient.post()
                .uri("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TestDataBuilders.createOtpRequest(phone))
                .exchange()
                .expectStatus().isOk

            // Verify OTP and get token
            val response = webTestClient.post()
                .uri("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TestDataBuilders.createOtpVerifyRequest(phone, otpCode))
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody!!

            val tokenResponse = ApiTestUtils.fromJson<Map<String, Any>>(response)
            return tokenResponse["accessToken"] as String
        }

        /**
         * Test CRUD operations
         */
        fun testCrudOperations(
            webTestClient: WebTestClient,
            basePath: String,
            createData: Any,
            updateData: Any,
            authToken: String? = null
        ): Long {
            val headers = if (authToken != null) {
                ApiTestUtils.createAuthenticatedHeaders(authToken)
            } else {
                ApiTestUtils.createTestHeaders()
            }

            // Create
            val createResponse = webTestClient.post()
                .uri(basePath)
                .contentType(MediaType.APPLICATION_JSON)
                .headers { h -> headers.forEach { (k, v) -> h.add(k, v) } }
                .bodyValue(createData)
                .exchange()
                .expectStatus().isCreated
                .expectBody(String::class.java)
                .returnResult()
                .responseBody!!

            val createdEntity = ApiTestUtils.fromJson<Map<String, Any>>(createResponse)
            val entityId = (createdEntity["id"] as Number).toLong()

            // Read
            webTestClient.get()
                .uri("$basePath/$entityId")
                .headers { h -> headers.forEach { (k, v) -> h.add(k, v) } }
                .exchange()
                .expectStatus().isOk

            // Update
            webTestClient.put()
                .uri("$basePath/$entityId")
                .contentType(MediaType.APPLICATION_JSON)
                .headers { h -> headers.forEach { (k, v) -> h.add(k, v) } }
                .bodyValue(updateData)
                .exchange()
                .expectStatus().isOk

            // Delete
            webTestClient.delete()
                .uri("$basePath/$entityId")
                .headers { h -> headers.forEach { (k, v) -> h.add(k, v) } }
                .exchange()
                .expectStatus().isNoContent

            return entityId
        }
    }
}

/**
 * Performance test utilities
 */
class PerformanceTestUtils {

    companion object {

        /**
         * Measure execution time
         */
        inline fun <T> measureTime(block: () -> T): Pair<T, Long> {
            val startTime = System.currentTimeMillis()
            val result = block()
            val endTime = System.currentTimeMillis()
            return Pair(result, endTime - startTime)
        }

        /**
         * Run load test
         */
        fun runLoadTest(
            testName: String,
            concurrency: Int,
            iterations: Int,
            testBlock: () -> Unit
        ): LoadTestResult {
            val results = mutableListOf<Long>()
            val errors = mutableListOf<Exception>()

            val startTime = System.currentTimeMillis()

            // Run tests concurrently
            val threads = (1..concurrency).map { threadId ->
                Thread {
                    repeat(iterations) { iteration ->
                        try {
                            val (_, duration) = measureTime { testBlock() }
                            synchronized(results) {
                                results.add(duration)
                            }
                        } catch (e: Exception) {
                            synchronized(errors) {
                                errors.add(e)
                            }
                        }
                    }
                }
            }

            threads.forEach { it.start() }
            threads.forEach { it.join() }

            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime

            return LoadTestResult(
                testName = testName,
                concurrency = concurrency,
                iterations = iterations,
                totalRequests = concurrency * iterations,
                successfulRequests = results.size,
                failedRequests = errors.size,
                totalTimeMs = totalTime,
                averageTimeMs = if (results.isNotEmpty()) results.average() else 0.0,
                minTimeMs = results.minOrNull() ?: 0L,
                maxTimeMs = results.maxOrNull() ?: 0L,
                requestsPerSecond = if (totalTime > 0) (results.size * 1000.0) / totalTime else 0.0,
                errors = errors
            )
        }
    }
}

/**
 * Load test result data class
 */
data class LoadTestResult(
    val testName: String,
    val concurrency: Int,
    val iterations: Int,
    val totalRequests: Int,
    val successfulRequests: Int,
    val failedRequests: Int,
    val totalTimeMs: Long,
    val averageTimeMs: Double,
    val minTimeMs: Long,
    val maxTimeMs: Long,
    val requestsPerSecond: Double,
    val errors: List<Exception>
) {

    fun printSummary() {
        println("=== Load Test Results: $testName ===")
        println("Concurrency: $concurrency")
        println("Iterations per thread: $iterations")
        println("Total requests: $totalRequests")
        println("Successful requests: $successfulRequests")
        println("Failed requests: $failedRequests")
        println("Success rate: ${(successfulRequests.toDouble() / totalRequests * 100).format(2)}%")
        println("Total time: ${totalTimeMs}ms")
        println("Average response time: ${averageTimeMs.format(2)}ms")
        println("Min response time: ${minTimeMs}ms")
        println("Max response time: ${maxTimeMs}ms")
        println("Requests per second: ${requestsPerSecond.format(2)}")

        if (errors.isNotEmpty()) {
            println("Errors:")
            errors.groupBy { it.javaClass.simpleName }
                .forEach { (errorType, errorList) ->
                    println("  $errorType: ${errorList.size}")
                }
        }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}