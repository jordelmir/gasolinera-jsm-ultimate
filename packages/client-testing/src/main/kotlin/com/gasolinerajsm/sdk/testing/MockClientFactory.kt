package com.gasolinerajsm.sdk.testing

import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

/**
 * Mock client factory for testing
 */
@Configuration
@Profile("test")
class MockClientFactory {

    /**
     * Mock Auth API client
     */
    @Bean
    @Primary
    fun mockAuthApi(): Any { // Replace with actual AuthApi when generated
        return mockk<Any>(relaxed = true)
    }

    /**
     * Mock Station API client
     */
    @Bean
    @Primary
    fun mockStationApi(): Any { // Replace with actual StationApi when generated
        return mockk<Any>(relaxed = true)
    }

    /**
     * Mock Coupon API client
     */
    @Bean
    @Primary
    fun mockCouponApi(): Any { // Replace with actual CouponApi when generated
        return mockk<Any>(relaxed = true)
    }

    /**
     * Mock Redemption API client
     */
    @Bean
    @Primary
    fun mockRedemptionApi(): Any { // Replace with actual RedemptionApi when generated
        return mockk<Any>(relaxed = true)
    }

    /**
     * Mock Ad Engine API client
     */
    @Bean
    @Primary
    fun mockAdEngineApi(): Any { // Replace with actual AdEngineApi when generated
        return mockk<Any>(relaxed = true)
    }

    /**
     * Mock Raffle API client
     */
    @Bean
    @Primary
    fun mockRaffleApi(): Any { // Replace with actual RaffleApi when generated
        return mockk<Any>(relaxed = true)
    }
}

/**
 * Utility class for creating mock clients
 */
class MockClientUtils {

    companion object {

        /**
         * Create a relaxed mock of any client
         */
        inline fun <reified T : Any> createMockClient(): T {
            return mockk<T>(relaxed = true)
        }

        /**
         * Create a strict mock of any client
         */
        inline fun <reified T : Any> createStrictMockClient(): T {
            return mockk<T>(relaxed = false)
        }

        /**
         * Create mock with custom behavior
         */
        inline fun <reified T : Any> createMockClientWithBehavior(
            relaxed: Boolean = true,
            block: T.() -> Unit = {}
        ): T {
            val mock = mockk<T>(relaxed = relaxed)
            mock.block()
            return mock
        }
    }
}

/**
 * Test data builders for common API models
 */
class TestDataBuilders {

    companion object {

        /**
         * Create test OTP request
         */
        fun createOtpRequest(phone: String = "+1234567890"): Map<String, Any> {
            return mapOf(
                "phone" to phone
            )
        }

        /**
         * Create test OTP verify request
         */
        fun createOtpVerifyRequest(
            phone: String = "+1234567890",
            code: String = "123456"
        ): Map<String, Any> {
            return mapOf(
                "phone" to phone,
                "code" to code
            )
        }

        /**
         * Create test token response
         */
        fun createTokenResponse(
            accessToken: String = "test-access-token",
            refreshToken: String = "test-refresh-token",
            expiresIn: Long = 3600
        ): Map<String, Any> {
            return mapOf(
                "accessToken" to accessToken,
                "refreshToken" to refreshToken,
                "expiresIn" to expiresIn,
                "tokenType" to "Bearer"
            )
        }

        /**
         * Create test station DTO
         */
        fun createStationDto(
            id: Long = 1L,
            name: String = "Test Station",
            address: String = "123 Test St",
            latitude: Double = 9.9281,
            longitude: Double = -84.0907
        ): Map<String, Any> {
            return mapOf(
                "id" to id,
                "name" to name,
                "address" to address,
                "latitude" to latitude,
                "longitude" to longitude,
                "active" to true
            )
        }

        /**
         * Create test coupon DTO
         */
        fun createCouponDto(
            id: Long = 1L,
            qrCode: String = "TEST-QR-CODE",
            amount: Double = 5000.0,
            stationId: Long = 1L
        ): Map<String, Any> {
            return mapOf(
                "id" to id,
                "qrCode" to qrCode,
                "amount" to amount,
                "stationId" to stationId,
                "active" to true,
                "createdAt" to "2024-01-01T00:00:00Z"
            )
        }

        /**
         * Create test redemption DTO
         */
        fun createRedemptionDto(
            id: Long = 1L,
            couponId: Long = 1L,
            userId: Long = 1L,
            points: Int = 1
        ): Map<String, Any> {
            return mapOf(
                "id" to id,
                "couponId" to couponId,
                "userId" to userId,
                "points" to points,
                "redeemedAt" to "2024-01-01T00:00:00Z"
            )
        }

        /**
         * Create test ad DTO
         */
        fun createAdDto(
            id: Long = 1L,
            title: String = "Test Ad",
            content: String = "Test ad content",
            duration: Int = 30
        ): Map<String, Any> {
            return mapOf(
                "id" to id,
                "title" to title,
                "content" to content,
                "duration" to duration,
                "active" to true
            )
        }

        /**
         * Create test raffle DTO
         */
        fun createRaffleDto(
            id: Long = 1L,
            name: String = "Weekly Raffle",
            prize: String = "₡40,000",
            drawDate: String = "2024-12-31T23:59:59Z"
        ): Map<String, Any> {
            return mapOf(
                "id" to id,
                "name" to name,
                "prize" to prize,
                "drawDate" to drawDate,
                "active" to true
            )
        }
    }
}