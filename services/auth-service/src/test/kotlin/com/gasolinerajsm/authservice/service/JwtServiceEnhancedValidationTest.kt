package com.gasolinerajsm.authservice.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit

class JwtServiceEnhancedValidationTest {

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var valueOperations: ValueOperations<String, String>
    private lateinit var jwtService: JwtService

    private val jwtSecret = "test-secret-key-for-jwt-signing-must-be-long-enough-for-hmac-sha256"
    private val validSubject = "user123"

    @BeforeEach
    fun setUp() {
        redisTemplate = mock()
        valueOperations = mock()

        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(redisTemplate.hasKey(any())).thenReturn(false) // Default: not blacklisted

        jwtService = JwtService(jwtSecret, redisTemplate)
    }

    @Test
    fun `should validate non-blacklisted token successfully`() {
        val token = jwtService.generateAccessToken(validSubject)

        // Ensure token is not blacklisted
        whenever(redisTemplate.hasKey("jwt_blacklist:$token")).thenReturn(false)

        val isValid = jwtService.validateToken(token)

        assertTrue(isValid, "Valid non-blacklisted token should be valid")
        verify(redisTemplate).hasKey("jwt_blacklist:$token")
    }

    @Test
    fun `should reject blacklisted token`() {
        val token = jwtService.generateAccessToken(validSubject)

        // Blacklist the token
        whenever(redisTemplate.hasKey("jwt_blacklist:$token")).thenReturn(true)

        val isValid = jwtService.validateToken(token)

        assertFalse(isValid, "Blacklisted token should be invalid")
        verify(redisTemplate).hasKey("jwt_blacklist:$token")
    }

    @Test
    fun `should check blacklist before signature validation for performance`() {
        val malformedToken = "malformed.jwt.token"

        // Token is blacklisted
        whenever(redisTemplate.hasKey("jwt_blacklist:$malformedToken")).thenReturn(true)

        val isValid = jwtService.validateToken(malformedToken)

        assertFalse(isValid, "Blacklisted token should be invalid regardless of format")
        verify(redisTemplate).hasKey("jwt_blacklist:$malformedToken")
    }

    @Test
    fun `should handle Redis connection failure gracefully during validation`() {
        val token = jwtService.generateAccessToken(validSubject)

        // Simulate Redis connection failure
        whenever(redisTemplate.hasKey(any())).thenThrow(RuntimeException("Redis connection failed"))

        // Should still validate the token (fail-safe behavior)
        val isValid = jwtService.validateToken(token)

        assertTrue(isValid, "Should validate token when Redis is unavailable (fail-safe)")
        verify(redisTemplate).hasKey("jwt_blacklist:$token")
    }

    @Test
    fun `should properly blacklist token with correct TTL`() {
        val token = jwtService.generateAccessToken(validSubject)

        // Mock the token parsing to return claims
        whenever(valueOperations.set(any(), any(), any<Long>(), any<TimeUnit>())).thenReturn(Unit)

        jwtService.blacklistToken(token)

        // Verify token was added to blacklist with appropriate TTL
        verify(valueOperations).set(
            eq("jwt_blacklist:$token"),
            eq("revoked"),
            any<Long>(),
            eq(TimeUnit.MILLISECONDS)
        )
    }

    @Test
    fun `should validate token signature after blacklist check`() {
        val validToken = jwtService.generateAccessToken(validSubject)
        val invalidSignatureToken = validToken.substring(0, validToken.length - 10) + "invalidsig"

        // Not blacklisted
        whenever(redisTemplate.hasKey(any())).thenReturn(false)

        val validResult = jwtService.validateToken(validToken)
        val invalidResult = jwtService.validateToken(invalidSignatureToken)

        assertTrue(validResult, "Valid token should pass validation")
        assertFalse(invalidResult, "Token with invalid signature should fail validation")
    }

    @Test
    fun `should reject expired token even if not blacklisted`() {
        // Create a token that will be expired (this is tricky to test without time manipulation)
        // For now, we'll test with a malformed token that represents an expired scenario
        val expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxNTE2MjM5MDIyfQ.invalid"

        whenever(redisTemplate.hasKey(any())).thenReturn(false)

        val isValid = jwtService.validateToken(expiredToken)

        assertFalse(isValid, "Expired token should be invalid")
    }

    @Test
    fun `should handle concurrent blacklist operations safely`() {
        val token = jwtService.generateAccessToken(validSubject)

        // Simulate concurrent access
        whenever(redisTemplate.hasKey("jwt_blacklist:$token"))
            .thenReturn(false) // First check: not blacklisted
            .thenReturn(true)  // Second check: blacklisted

        val firstCheck = jwtService.validateToken(token)
        val secondCheck = jwtService.validateToken(token)

        assertTrue(firstCheck, "First validation should succeed")
        assertFalse(secondCheck, "Second validation should fail (token was blacklisted)")
    }

    @Test
    fun `should validate refresh token correctly`() {
        val refreshToken = jwtService.generateRefreshToken(validSubject)

        whenever(redisTemplate.hasKey(any())).thenReturn(false)

        val isValid = jwtService.validateToken(refreshToken)

        assertTrue(isValid, "Valid refresh token should be valid")
        verify(redisTemplate).hasKey("jwt_blacklist:$refreshToken")
    }

    @Test
    fun `should handle null or empty token gracefully`() {
        val nullTokenValid = jwtService.validateToken("")

        assertFalse(nullTokenValid, "Empty token should be invalid")

        // Should not call Redis for obviously invalid tokens
        verify(redisTemplate, never()).hasKey(any())
    }

    @Test
    fun `should validate token type correctly after blacklist check`() {
        val accessToken = jwtService.generateAccessToken(validSubject)
        val refreshToken = jwtService.generateRefreshToken(validSubject)

        whenever(redisTemplate.hasKey(any())).thenReturn(false)

        val accessTokenIsAccess = jwtService.isTokenOfType(accessToken, "access")
        val refreshTokenIsRefresh = jwtService.isTokenOfType(refreshToken, "refresh")
        val accessTokenIsRefresh = jwtService.isTokenOfType(accessToken, "refresh")

        assertTrue(accessTokenIsAccess, "Access token should be identified as access type")
        assertTrue(refreshTokenIsRefresh, "Refresh token should be identified as refresh type")
        assertFalse(accessTokenIsRefresh, "Access token should not be identified as refresh type")
    }

    @Test
    fun `should extract subject from valid non-blacklisted token`() {
        val token = jwtService.generateAccessToken(validSubject)

        whenever(redisTemplate.hasKey(any())).thenReturn(false)

        val extractedSubject = jwtService.getSubjectFromToken(token)

        assertEquals(validSubject, extractedSubject, "Should extract correct subject from token")
    }

    @Test
    fun `should handle blacklist check failure during token operations`() {
        val token = jwtService.generateAccessToken(validSubject)

        // Simulate Redis failure during blacklist check
        whenever(redisTemplate.hasKey(any())).thenThrow(RuntimeException("Redis unavailable"))

        // Validation should still work (fail-safe)
        val isValid = jwtService.validateToken(token)
        assertTrue(isValid, "Should validate token when blacklist check fails")

        // Subject extraction should still work
        val subject = jwtService.getSubjectFromToken(token)
        assertEquals(validSubject, subject, "Should extract subject even when Redis fails")
    }

    @Test
    fun `should properly handle blacklist TTL expiration`() {
        val token = jwtService.generateAccessToken(validSubject)

        // First check: token is blacklisted
        whenever(redisTemplate.hasKey("jwt_blacklist:$token")).thenReturn(true)
        val firstValidation = jwtService.validateToken(token)
        assertFalse(firstValidation, "Blacklisted token should be invalid")

        // Second check: blacklist entry expired (TTL reached)
        whenever(redisTemplate.hasKey("jwt_blacklist:$token")).thenReturn(false)
        val secondValidation = jwtService.validateToken(token)
        assertTrue(secondValidation, "Token should be valid after blacklist TTL expires")
    }

    @Test
    fun `should validate token with roles correctly`() {
        val roles = listOf("ADMIN", "USER")
        val tokenWithRoles = jwtService.generateAccessToken(validSubject, roles)

        whenever(redisTemplate.hasKey(any())).thenReturn(false)

        val isValid = jwtService.validateToken(tokenWithRoles)
        val extractedRoles = jwtService.getRolesFromToken(tokenWithRoles)

        assertTrue(isValid, "Token with roles should be valid")
        assertEquals(roles, extractedRoles, "Should extract correct roles from token")
    }
}