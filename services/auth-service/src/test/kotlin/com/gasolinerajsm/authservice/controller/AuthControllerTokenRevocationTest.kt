package com.gasolinerajsm.authservice.controller

import com.gasolinerajsm.authservice.application.AuthenticationUseCase
import com.gasolinerajsm.authservice.service.AuthService
import com.gasolinerajsm.authservice.service.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AuthController::class)
class AuthControllerTokenRevocationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authenticationUseCase: AuthenticationUseCase

    @MockBean
    private lateinit var authService: AuthService

    @MockBean
    private lateinit var jwtService: JwtService

    @MockBean
    private lateinit var environment: Environment

    private val validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    @BeforeEach
    fun setUp() {
        reset(authService, jwtService)
    }

    @Test
    fun `should revoke valid token successfully`() {
        whenever(jwtService.validateToken(validToken)).thenReturn(true)
        doNothing().whenever(authService).revokeToken(validToken)

        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Token revoked successfully"))

        verify(jwtService).validateToken(validToken)
        verify(authService).revokeToken(validToken)
    }

    @Test
    fun `should reject revocation request without Bearer prefix`() {
        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Invalid authorization header format. Use 'Bearer <token>'"))

        verify(jwtService, never()).validateToken(any())
        verify(authService, never()).revokeToken(any())
    }

    @Test
    fun `should reject revocation request with empty token`() {
        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer ")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Token cannot be empty"))

        verify(jwtService, never()).validateToken(any())
        verify(authService, never()).revokeToken(any())
    }

    @Test
    fun `should reject revocation request without Authorization header`() {
        mockMvc.perform(
            post("/auth/revoke")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should reject invalid token for revocation`() {
        val invalidToken = "invalid.token.here"
        whenever(jwtService.validateToken(invalidToken)).thenReturn(false)

        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer $invalidToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Invalid or expired token"))

        verify(jwtService).validateToken(invalidToken)
        verify(authService, never()).revokeToken(any())
    }

    @Test
    fun `should handle token validation exception during revocation`() {
        whenever(jwtService.validateToken(validToken)).thenThrow(IllegalArgumentException("Malformed token"))

        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Invalid token format"))

        verify(jwtService).validateToken(validToken)
        verify(authService, never()).revokeToken(any())
    }

    @Test
    fun `should handle service exception during token revocation`() {
        whenever(jwtService.validateToken(validToken)).thenReturn(true)
        doThrow(RuntimeException("Redis connection failed")).whenever(authService).revokeToken(validToken)

        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Token revocation failed"))

        verify(jwtService).validateToken(validToken)
        verify(authService).revokeToken(validToken)
    }

    @Test
    fun `should logout with valid token successfully`() {
        whenever(jwtService.validateToken(validToken)).thenReturn(true)
        doNothing().whenever(authService).revokeToken(validToken)

        mockMvc.perform(
            post("/auth/logout")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Logged out successfully"))

        verify(jwtService).validateToken(validToken)
        verify(authService).revokeToken(validToken)
    }

    @Test
    fun `should reject logout request with invalid authorization header`() {
        mockMvc.perform(
            post("/auth/logout")
                .header("Authorization", "InvalidFormat $validToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Invalid authorization header format. Use 'Bearer <token>'"))

        verify(jwtService, never()).validateToken(any())
        verify(authService, never()).revokeToken(any())
    }

    @Test
    fun `should reject logout with expired token`() {
        val expiredToken = "expired.jwt.token"
        whenever(jwtService.validateToken(expiredToken)).thenReturn(false)

        mockMvc.perform(
            post("/auth/logout")
                .header("Authorization", "Bearer $expiredToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Invalid or expired token"))

        verify(jwtService).validateToken(expiredToken)
        verify(authService, never()).revokeToken(any())
    }

    @Test
    fun `should handle concurrent revocation requests gracefully`() {
        whenever(jwtService.validateToken(validToken)).thenReturn(true)
        doNothing().whenever(authService).revokeToken(validToken)

        // Simulate multiple concurrent requests
        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Token revoked successfully"))

        // Second request should also succeed (idempotent operation)
        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Token revoked successfully"))

        verify(jwtService, times(2)).validateToken(validToken)
        verify(authService, times(2)).revokeToken(validToken)
    }

    @Test
    fun `should validate token before revocation to prevent unnecessary operations`() {
        val malformedToken = "not.a.valid.jwt"
        whenever(jwtService.validateToken(malformedToken)).thenReturn(false)

        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer $malformedToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Invalid or expired token"))

        verify(jwtService).validateToken(malformedToken)
        verify(authService, never()).revokeToken(any())
    }

    @Test
    fun `should handle whitespace in token gracefully`() {
        val tokenWithSpaces = "  $validToken  "
        whenever(jwtService.validateToken(validToken)).thenReturn(true)
        doNothing().whenever(authService).revokeToken(validToken)

        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer $tokenWithSpaces")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Token revoked successfully"))

        verify(jwtService).validateToken(tokenWithSpaces.trim())
        verify(authService).revokeToken(tokenWithSpaces.trim())
    }

    @Test
    fun `should return appropriate error for blacklisted token during revocation`() {
        val blacklistedToken = "already.blacklisted.token"
        whenever(jwtService.validateToken(blacklistedToken)).thenReturn(false) // Already blacklisted tokens are invalid

        mockMvc.perform(
            post("/auth/revoke")
                .header("Authorization", "Bearer $blacklistedToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Invalid or expired token"))

        verify(jwtService).validateToken(blacklistedToken)
        verify(authService, never()).revokeToken(any())
    }
}