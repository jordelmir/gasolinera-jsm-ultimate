package com.gasolinerajsm.sdk.testing

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException

class ApiTestUtilsTest {

    @Test
    fun `should convert object to JSON and back`() {
        val testData = mapOf(
            "id" to 1,
            "name" to "Test",
            "active" to true
        )

        val json = ApiTestUtils.toJson(testData)
        val result = ApiTestUtils.fromJson<Map<String, Any>>(json)

        assertEquals(testData["id"], result["id"])
        assertEquals(testData["name"], result["name"])
        assertEquals(testData["active"], result["active"])
    }

    @Test
    fun `should create WebClientResponseException`() {
        val exception = ApiTestUtils.createWebClientException(
            HttpStatus.BAD_REQUEST,
            "Test error",
            "Error body"
        )

        assertEquals(400, exception.statusCode.value())
        assertEquals("400 Test error", exception.message)
        assertEquals("Error body", String(exception.responseBodyAsByteArray))
    }

    @Test
    fun `should mock successful response`() {
        val mockClient = mockk<TestClient>()
        val expectedResponse = "success"

        ApiTestUtils.mockSuccessfulResponse(mockClient, { getData() }, expectedResponse)

        val result = mockClient.getData()
        assertEquals(expectedResponse, result)
    }

    @Test
    fun `should mock error response`() {
        val mockClient = mockk<TestClient>()

        ApiTestUtils.mockErrorResponse(
            mockClient,
            { getData() },
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Server error"
        )

        val exception = assertThrows<WebClientResponseException> {
            mockClient.getData()
        }

        assertEquals(500, exception.statusCode.value())
        assertEquals("500 Server error", exception.message)
    }

    @Test
    fun `should assert response structure`() {
        val response = mapOf(
            "id" to 1,
            "name" to "Test",
            "active" to true
        )

        val expectedFields = listOf("id", "name", "active")

        // Should not throw
        ApiTestUtils.assertResponseStructure(response, expectedFields)
    }

    @Test
    fun `should fail when response structure is invalid`() {
        val response = mapOf(
            "id" to 1,
            "name" to "Test"
        )

        val expectedFields = listOf("id", "name", "active")

        assertThrows<AssertionError> {
            ApiTestUtils.assertResponseStructure(response, expectedFields)
        }
    }

    @Test
    fun `should create test headers`() {
        val headers = ApiTestUtils.createTestHeaders()

        assertTrue(headers.containsKey("Content-Type"))
        assertTrue(headers.containsKey("Accept"))
        assertTrue(headers.containsKey("X-Test-Client"))
        assertEquals("true", headers["X-Test-Client"])
    }

    @Test
    fun `should create authenticated headers`() {
        val token = "test-token"
        val headers = ApiTestUtils.createAuthenticatedHeaders(token)

        assertTrue(headers.containsKey("Authorization"))
        assertEquals("Bearer $token", headers["Authorization"])
        assertTrue(headers.containsKey("Content-Type"))
        assertTrue(headers.containsKey("Accept"))
    }

    @Test
    fun `should run async test`() {
        val result = ApiTestUtils.runAsyncTest {
            // Simulate async operation
            kotlinx.coroutines.delay(10)
            "async result"
        }

        assertEquals("async result", result)
    }

    // Test client interface for mocking
    interface TestClient {
        fun getData(): String
    }
}

class TestDataBuildersTest {

    @Test
    fun `should create OTP request`() {
        val request = TestDataBuilders.createOtpRequest("+1234567890")

        assertEquals("+1234567890", request["phone"])
    }

    @Test
    fun `should create OTP verify request`() {
        val request = TestDataBuilders.createOtpVerifyRequest("+1234567890", "123456")

        assertEquals("+1234567890", request["phone"])
        assertEquals("123456", request["code"])
    }

    @Test
    fun `should create token response`() {
        val response = TestDataBuilders.createTokenResponse(
            "access-token",
            "refresh-token",
            3600
        )

        assertEquals("access-token", response["accessToken"])
        assertEquals("refresh-token", response["refreshToken"])
        assertEquals(3600L, response["expiresIn"])
        assertEquals("Bearer", response["tokenType"])
    }

    @Test
    fun `should create station DTO`() {
        val station = TestDataBuilders.createStationDto(
            1L,
            "Test Station",
            "123 Test St",
            9.9281,
            -84.0907
        )

        assertEquals(1L, station["id"])
        assertEquals("Test Station", station["name"])
        assertEquals("123 Test St", station["address"])
        assertEquals(9.9281, station["latitude"])
        assertEquals(-84.0907, station["longitude"])
        assertEquals(true, station["active"])
    }

    @Test
    fun `should create coupon DTO`() {
        val coupon = TestDataBuilders.createCouponDto(
            1L,
            "TEST-QR-CODE",
            5000.0,
            1L
        )

        assertEquals(1L, coupon["id"])
        assertEquals("TEST-QR-CODE", coupon["qrCode"])
        assertEquals(5000.0, coupon["amount"])
        assertEquals(1L, coupon["stationId"])
        assertEquals(true, coupon["active"])
        assertNotNull(coupon["createdAt"])
    }

    @Test
    fun `should create redemption DTO`() {
        val redemption = TestDataBuilders.createRedemptionDto(
            1L,
            1L,
            1L,
            1
        )

        assertEquals(1L, redemption["id"])
        assertEquals(1L, redemption["couponId"])
        assertEquals(1L, redemption["userId"])
        assertEquals(1, redemption["points"])
        assertNotNull(redemption["redeemedAt"])
    }

    @Test
    fun `should create ad DTO`() {
        val ad = TestDataBuilders.createAdDto(
            1L,
            "Test Ad",
            "Test ad content",
            30
        )

        assertEquals(1L, ad["id"])
        assertEquals("Test Ad", ad["title"])
        assertEquals("Test ad content", ad["content"])
        assertEquals(30, ad["duration"])
        assertEquals(true, ad["active"])
    }

    @Test
    fun `should create raffle DTO`() {
        val raffle = TestDataBuilders.createRaffleDto(
            1L,
            "Weekly Raffle",
            "₡40,000",
            "2024-12-31T23:59:59Z"
        )

        assertEquals(1L, raffle["id"])
        assertEquals("Weekly Raffle", raffle["name"])
        assertEquals("₡40,000", raffle["prize"])
        assertEquals("2024-12-31T23:59:59Z", raffle["drawDate"])
        assertEquals(true, raffle["active"])
    }
}

class PerformanceTestUtilsTest {

    @Test
    fun `should measure execution time`() {
        val (result, duration) = PerformanceTestUtils.measureTime {
            Thread.sleep(100) // Simulate work
            "test result"
        }

        assertEquals("test result", result)
        assertTrue(duration >= 100) // Should be at least 100ms
    }

    @Test
    fun `should run load test`() {
        var counter = 0

        val result = PerformanceTestUtils.runLoadTest(
            testName = "Test Load Test",
            concurrency = 2,
            iterations = 3
        ) {
            synchronized(this) {
                counter++
            }
            Thread.sleep(10) // Simulate work
        }

        assertEquals("Test Load Test", result.testName)
        assertEquals(2, result.concurrency)
        assertEquals(3, result.iterations)
        assertEquals(6, result.totalRequests)
        assertEquals(6, result.successfulRequests)
        assertEquals(0, result.failedRequests)
        assertTrue(result.totalTimeMs > 0)
        assertTrue(result.averageTimeMs > 0)
        assertTrue(result.requestsPerSecond > 0)
        assertEquals(6, counter) // All iterations should have executed
    }

    @Test
    fun `should handle errors in load test`() {
        val result = PerformanceTestUtils.runLoadTest(
            testName = "Error Test",
            concurrency = 1,
            iterations = 2
        ) {
            throw RuntimeException("Test error")
        }

        assertEquals(0, result.successfulRequests)
        assertEquals(2, result.failedRequests)
        assertEquals(2, result.errors.size)
    }
}