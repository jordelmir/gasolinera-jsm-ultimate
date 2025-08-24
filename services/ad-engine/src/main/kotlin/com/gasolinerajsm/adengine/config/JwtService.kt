
package com.gasolinerajsm.adengine.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {

    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    @Value("\${app.jwt.secret:defaultSecretKeyForDevelopment}")
    private lateinit var secret: String

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    /**
     * Extracts all claims from JWT token
     * @param token JWT token string
     * @return Claims object containing all token claims
     * @throws JwtException if token is invalid or expired
     */
    fun extractAllClaims(token: String): Claims {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            logger.warn("JWT token is expired: {}", e.message)
            throw e
        } catch (e: JwtException) {
            logger.error("Invalid JWT token: {}", e.message)
            throw e
        }
    }

    /**
     * Extracts username from JWT token
     * @param token JWT token string
     * @return Username or empty string if not found
     */
    fun getUsername(token: String): String {
        return try {
            extractAllClaims(token).subject ?: ""
        } catch (e: Exception) {
            logger.error("Error extracting username from token: {}", e.message)
            ""
        }
    }

    /**
     * Extracts user ID from JWT token
     * @param token JWT token string
     * @return User ID or null if not found
     */
    fun getUserId(token: String): String? {
        return try {
            val claims = extractAllClaims(token)
            claims["userId"] as? String
        } catch (e: Exception) {
            logger.error("Error extracting user ID from token: {}", e.message)
            null
        }
    }

    /**
     * Extracts roles from JWT token with safe casting
     * @param token JWT token string
     * @return List of role strings, empty list if none found or invalid
     */
    fun getRoles(token: String): List<String> {
        return try {
            val claims = extractAllClaims(token)
            val roles = claims["roles"]
            when (roles) {
                is List<*> -> roles.filterIsInstance<String>()
                is String -> listOf(roles)
                else -> {
                    logger.warn("Roles claim is not a valid list or string: {}", roles?.javaClass?.simpleName)
                    emptyList()
                }
            }
        } catch (e: Exception) {
            logger.error("Error extracting roles from token: {}", e.message)
            emptyList()
        }
    }

    /**
     * Extracts user type from JWT token
     * @param token JWT token string
     * @return User type or null if not found
     */
    fun getUserType(token: String): String? {
        return try {
            val claims = extractAllClaims(token)
            claims["userType"] as? String
        } catch (e: Exception) {
            logger.error("Error extracting user type from token: {}", e.message)
            null
        }
    }

    /**
     * Validates JWT token
     * @param token JWT token string
     * @return true if token is valid and not expired, false otherwise
     */
    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)

            // Check if token is expired
            val expiration = claims.expiration
            if (expiration != null && expiration.before(Date())) {
                logger.debug("Token is expired")
                return false
            }

            // Check if subject exists
            if (claims.subject.isNullOrBlank()) {
                logger.debug("Token has no subject")
                return false
            }

            true
        } catch (e: ExpiredJwtException) {
            logger.debug("Token is expired: {}", e.message)
            false
        } catch (e: JwtException) {
            logger.debug("Invalid token: {}", e.message)
            false
        } catch (e: Exception) {
            logger.error("Unexpected error validating token: {}", e.message)
            false
        }
    }

    /**
     * Validates JWT token for a specific username
     * @param token JWT token string
     * @param username Expected username
     * @return true if token is valid and matches username
     */
    fun isTokenValidForUser(token: String, username: String): Boolean {
        return try {
            isTokenValid(token) && getUsername(token) == username
        } catch (e: Exception) {
            logger.error("Error validating token for user {}: {}", username, e.message)
            false
        }
    }

    /**
     * Checks if user has required role
     * @param token JWT token string
     * @param requiredRole Required role to check
     * @return true if user has the required role
     */
    fun hasRole(token: String, requiredRole: String): Boolean {
        return try {
            val userRoles = getRoles(token)
            userRoles.contains(requiredRole)
        } catch (e: Exception) {
            logger.error("Error checking role {} for token: {}", requiredRole, e.message)
            false
        }
    }

    /**
     * Checks if user has any of the required roles
     * @param token JWT token string
     * @param requiredRoles List of acceptable roles
     * @return true if user has at least one of the required roles
     */
    fun hasAnyRole(token: String, requiredRoles: List<String>): Boolean {
        return try {
            val userRoles = getRoles(token)
            userRoles.any { role -> requiredRoles.contains(role) }
        } catch (e: Exception) {
            logger.error("Error checking roles {} for token: {}", requiredRoles, e.message)
            false
        }
    }
}
