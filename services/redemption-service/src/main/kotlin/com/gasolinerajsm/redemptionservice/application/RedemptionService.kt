package com.gasolinerajsm.redemptionservice.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.gasolinerajsm.redemptionservice.adapter.out.cdc.Outbox
import com.gasolinerajsm.redemptionservice.adapter.out.persistence.OutboxRepository
import com.gasolinerajsm.redemptionservice.domain.aggregate.Redemption
import com.gasolinerajsm.redemptionservice.domain.event.RedemptionInitiatedEvent
import com.gasolinerajsm.redemptionservice.domain.repository.RedemptionRepository
import com.gasolinerajsm.redemptionservice.service.QrSecurityService
import com.gasolinerajsm.redemptionservice.domain.model.PointsLedgerEntry
import com.gasolinerajsm.redemptionservice.domain.repository.PointsLedgerRepository
// import com.gasolinerajsm.sdk.adengine.api.AdApi
// import com.gasolinerajsm.sdk.adengine.model.AdSelectionRequest
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Counter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

// Temporary mock class until SDK is ready
data class MockAdCreative(
    val creative_url: String,
    val impression_url: String
)

@Service
class RedemptionService(
    private val qrSecurityService: QrSecurityService,
    private val redemptionRepository: RedemptionRepository,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper,
    // private val adApi: AdApi, // Refactored to use AdApi SDK
    private val redisTemplate: StringRedisTemplate,
    private val meterRegistry: MeterRegistry,
    private val pointsLedgerRepository: PointsLedgerRepository
) {

    private val redemptionsSuccessfulTotal: Counter = meterRegistry.counter("redemptions_successful_total")
    private val redemptionsFailedTotal: Counter = meterRegistry.counter("redemptions_failed_total")

    private val logger = LoggerFactory.getLogger(RedemptionService::class.java)

    @Transactional
    fun initiateRedemption(command: RedeemCommand): RedemptionResult {
        logger.info("Initiating redemption for userId: {} with QR token: {}", command.userId, command.qrToken)

        val verifiedQr = try {
            qrSecurityService.validateAndParseToken(command.qrToken)
        } catch (e: Exception) {
            logger.error("QR token verification failed for user {}: {}", command.userId, e.message)
            redemptionsFailedTotal.increment()
            throw e
        }

        logger.debug("QR token successfully verified for nonce: {}", verifiedQr.n)

        val redemption = Redemption.initiate(
            userId = command.userId,
            qr = verifiedQr
        )

        // 2. Mock ad selection for now (will be replaced with SDK later)
        val adCreative = MockAdCreative(
            creative_url = "https://example.com/ad-creative.mp4",
            impression_url = "https://example.com/impression-track"
        )

        redemptionRepository.save(redemption)
        logger.info("Redemption aggregate saved with ID: {}", redemption.id)

        // 4. Almacenar datos de sesión en Redis
        val sessionId = redemption.id.toString()
        val sessionData = mapOf(
            "userId" to command.userId,
            "impressionUrl" to adCreative.impression_url
        )
        redisTemplate.opsForHash<String, String>().putAll(sessionId, sessionData)
        redisTemplate.expire(sessionId, 10, TimeUnit.MINUTES)
        logger.info("Session data stored in Redis for sessionId: {}", sessionId)

        val event = RedemptionInitiatedEvent(
            redemptionId = redemption.id,
            userId = redemption.userId,
            stationId = redemption.stationId
        )
        val outboxEvent = Outbox(
            aggregateType = "redemption",
            aggregateId = redemption.id.toString(),
            eventType = event.javaClass.simpleName,
            payload = objectMapper.writeValueAsString(event)
        )
        outboxRepository.save(outboxEvent)
        logger.info("RedemptionInitiatedEvent saved to outbox with ID: {}", outboxEvent.id)

        redemptionsSuccessfulTotal.increment()

        // 6. Devolver resultado
        // Nota: campaignId y creativeId no están en el nuevo AdCreative.
        // Se omite por ahora. Si son necesarios, se debe actualizar el AdEngine.
        return RedemptionResult(
            redemptionId = redemption.id,
            status = "PENDING_AD_VIEW",
            adUrl = adCreative.creative_url,
            campaignId = 0, // Dato no disponible
            creativeId = "" // Dato no disponible
        )
    }

    @Transactional
    fun confirmAdWatched(request: ConfirmAdRequest): ConfirmAdResponse {
        logger.info("Confirming ad watched for sessionId: {}", request.sessionId)
        val sessionId = request.sessionId
        val sessionData = redisTemplate.opsForHash<String, String>().entries(sessionId)

        val userId = sessionData["userId"] ?: run { logger.warn("Session data not found for sessionId: {}", sessionId); throw IllegalStateException("Session data not found for $sessionId") }
        val impressionUrl = sessionData["impressionUrl"] ?: run { logger.warn("Impression URL not found in session for sessionId: {}", sessionId); throw IllegalStateException("Impression URL not found in session for $sessionId") }

        logger.info("Recording ad impression for userId: {} (mock implementation)", userId)
        // Mock impression recording - will be replaced with actual API call

        val pointsToCredit = 25
        val redemptionId = UUID.fromString(sessionId)

        val pointsEntry = PointsLedgerEntry(
            userId = userId,
            pointsCredited = pointsToCredit,
            redemptionId = redemptionId
        )
        pointsLedgerRepository.save(pointsEntry)
        logger.info("Points credited: {} for userId: {} and redemptionId: {}", pointsToCredit, userId, redemptionId)

        logger.info("Ad watched confirmed and points accredited for sessionId: {}", sessionId)

        redisTemplate.delete(sessionId)
        logger.info("Session data deleted from Redis for sessionId: {}", sessionId)

        return ConfirmAdResponse(balance = 100) // Mock balance
    }

    fun countTotalPointsRedeemed(): Long {
        return pointsLedgerRepository.sumPointsCredited() // Assuming this method exists or will be created
    }
}
