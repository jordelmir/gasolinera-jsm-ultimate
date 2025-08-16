package com.gasolinerajsm.authservice.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey
import org.slf4j.LoggerFactory

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val jwtSecret: String
) {
    private val accessExpirationMs: Long = 15 * 60 * 1000 // 15 minutes
    private val refreshExpirationMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days

    private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    fun generateAccessToken(subject: String): String {
        val token = Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + accessExpirationMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
        logger.info("Generated access token for subject: {}", subject)
        return token
    }

    fun generateRefreshToken(subject: String): String {
        val token = Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + refreshExpirationMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
        logger.info("Generated refresh token for subject: {}", subject)
        return token
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            logger.debug("Token validated successfully.")
            true
        } catch (e: Exception) {
            logger.warn("Token validation failed: {}", e.message)
            false
        }
    }

    fun getSubjectFromToken(token: String): String {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body.subject
    }
}
