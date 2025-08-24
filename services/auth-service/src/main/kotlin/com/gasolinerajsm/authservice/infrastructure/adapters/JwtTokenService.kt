package com.gasolinerajsm.authservice.infrastructure.adapters

import com.gasolinerajsm.authservice.domain.Role
import com.gasolinerajsm.authservice.domain.UserId
import com.gasolinerajsm.authservice.domain.ports.TokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

/**
 * JWT implementation of TokenService port.
 * Handles JWT token generation and validation.
 */
@Service
class JwtTokenService(
    @Value("\${app.jwt.secret:defaultSecretKeyThatShouldBeChangedInProduction}")
    private val jwtSecret: String,

    @Value("\${app.jwt.access-token-expiration:3600000}") // 1 hour
    private val accessTokenExpiration: Long,

    @Value("\${app.jwt.refresh-token-expiration:604800000}") // 7 days
    private val refreshTokenExpiration: Long
) : TokenService {

    private val logger = LoggerFactory.getLogger(JwtTokenService::class.java)
    private val key: Key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    override fun generateAccessToken(userId: UserId, roles: Set<Role>): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("roles", roles.map { it.name })
            .claim("type", "access")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    override fun generateRefreshToken(userId: UserId): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpiration)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    override fun validateToken(token: String): UserId? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            UserId.from(claims.subject)
        } catch (e: Exception) {
            logger.warn("Invalid token: {}", e.message)
            null
        }
    }

    override fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Extract claims from token for internal use
     */
    private fun getClaimsFromToken(token: String): Claims? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) {
            logger.warn("Failed to extract claims from token: {}", e.message)
            null
        }
    }
}