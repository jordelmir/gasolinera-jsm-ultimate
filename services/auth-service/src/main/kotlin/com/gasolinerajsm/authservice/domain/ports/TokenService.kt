package com.gasolinerajsm.authservice.domain.ports

import com.gasolinerajsm.authservice.domain.Role
import com.gasolinerajsm.authservice.domain.UserId

/**
 * Port (interface) for token operations.
 * This abstracts JWT token generation and validation.
 */
interface TokenService {

    /**
     * Generate an access token for a user
     */
    fun generateAccessToken(userId: UserId, roles: Set<Role> = emptySet()): String

    /**
     * Generate a refresh token for a user
     */
    fun generateRefreshToken(userId: UserId): String

    /**
     * Validate and extract user ID from a token
     */
    fun validateToken(token: String): UserId?

    /**
     * Check if a token is expired
     */
    fun isTokenExpired(token: String): Boolean
}