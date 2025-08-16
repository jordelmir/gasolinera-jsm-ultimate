
package com.gasolinerajsm.redemptionservice.service

import com.gasolinerajsm.redemptionservice.controller.RedeemRequest
import com.gasolinerajsm.redemptionservice.controller.RedeemResponse
import com.gasolinerajsm.redemptionservice.controller.ConfirmAdRequest
import com.gasolinerajsm.redemptionservice.controller.ConfirmAdResponse
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedemptionService(
    private val redisTemplate: StringRedisTemplate
    // Inject other dependencies: StationServiceClient, AdEngineClient, PointRepository, RabbitTemplate etc.
) {

    @Value("\${app.qr.secret}")
    private lateinit var qrSecret: String

    @Value("\${app.geofence-radius-meters}")
    private lateinit var geofenceRadius: String

    fun initiateRedemption(userId: String, request: RedeemRequest): RedeemResponse {
        // 1. Verify QR Token Signature & Claims
        val claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(qrSecret.toByteArray()))
            .build()
            .parseClaimsJws(request.qr_token)
            .body
        
        val nonce = claims["n"] as String
        val stationId = claims["s"] as String

        // 2. Check for Nonce Replay in Redis (fast check) and DB (atomic check)
        // This should be a distributed lock + DB check
        val lockKey = "lock:qr:$nonce"
        if (redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS) != true) {
            throw IllegalStateException("Nonce already being processed or used.")
        }

        // 3. Geofencing Check
        // val stationLocation = stationServiceClient.getStation(stationId).location
        // val userLocation = request.gps
        // if (haversineDistance(stationLocation, userLocation) > geofenceRadius.toDouble()) {
        //     throw IllegalStateException("User is too far from the station.")
        // }

        // 4. Calculate Points
        val points = 100 // Dummy calculation

        // 5. Call Ad-Engine to get an Ad
        // val ad = adEngineClient.selectAd(userId, stationId)
        val ad = mapOf("creative_url" to "http://example.com/ad.mp4", "duration" to 15)

        // 6. Store context in Redis and return session ID
        val sessionId = "session-for-" + nonce
        redisTemplate.opsForValue().set("session:$sessionId", "$userId:$nonce:$points", 5, TimeUnit.MINUTES)

        return RedeemResponse(sessionId, ad, points)
    }

    fun confirmAdWatched(userId: String, request: ConfirmAdRequest): ConfirmAdResponse {
        val sessionData = redisTemplate.opsForValue().get("session:${request.session_id}")
            ?: throw IllegalStateException("Invalid or expired session.")
        
        val (sessionUserId, nonce, pointsStr) = sessionData.split(":")
        if (sessionUserId != userId) {
            throw SecurityException("User mismatch.")
        }

        // ATOMIC TRANSACTION:
        // 1. Mark nonce as used in PostgreSQL
        // 2. Credit points to user
        // 3. Commit
        
        // After successful transaction:
        redisTemplate.delete("session:${request.session_id}")
        redisTemplate.delete("lock:qr:$nonce")

        // Publish event to RabbitMQ
        // rabbitTemplate.convertAndSend("points_credited", ...)

        return ConfirmAdResponse(balance = 1000) // dummy balance
    }
}
