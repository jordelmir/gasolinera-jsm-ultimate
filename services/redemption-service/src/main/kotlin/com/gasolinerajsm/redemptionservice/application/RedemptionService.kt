package com.gasolinerajsm.redemptionservice.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit
// Asumiendo entidades y repositorios de DDD
import com.gasolinerajsm.redemptionservice.domain.aggregate.Redemption
import com.gasolinerajsm.redemptionservice.domain.event.RedemptionInitiatedEvent
import com.gasolinerajsm.redemptionservice.adapter.out.cdc.Outbox
import com.gasolinerajsm.redemptionservice.adapter.out.persistence.OutboxRepository
import com.gasolinerajsm.redemptionservice.service.QrSecurityService
import com.gasolinerajsm.redemptionservice.client.AdEngineClient

@Service
class RedemptionService(
    private val qrSecurityService: QrSecurityService,
    private val redemptionRepository: RedemptionRepository,
    private val outboxRepository: OutboxRepository, // Repositorio para la tabla outbox
    private val objectMapper: ObjectMapper,
    private val adEngineClient: AdEngineClient,
    private val redisTemplate: StringRedisTemplate
) {

    @Transactional
    fun initiateRedemption(command: RedeemCommand): RedemptionResult {
        // 1. Verificar firma ECDSA, nonce, geofencing (lógica movida a un Value Object)
        val verifiedQr = qrSecurityService.validateAndParseToken(command.qrToken)

        val redemption = Redemption.initiate(
            userId = command.userId,
            qr = verifiedQr
        )

        // 2. Seleccionar anuncio
        val adSelectionRequest = com.gasolinerajsm.redemptionservice.client.AdSelectionRequest(
            userId = command.userId.toLong(), // Assuming userId can be converted to Long
            stationId = verifiedQr.s.toLong() // Assuming stationId can be converted to Long
        )
        val adCreative = adEngineClient.selectAd(adSelectionRequest)
            ?: throw IllegalStateException("Failed to select ad") // Handle null response

        // 3. Persistir el estado del Agregado
        redemptionRepository.save(redemption)

        // 4. Almacenar datos de sesión en Redis para confirmación posterior
        val sessionId = redemption.id.toString()
        val sessionData = mapOf(
            "userId" to command.userId,
            "campaignId" to adCreative.campaignId.toString(),
            "creativeId" to adCreative.creativeId
        )
        redisTemplate.opsForHash<String, String>().putAll(sessionId, sessionData)
        redisTemplate.expire(sessionId, 10, TimeUnit.MINUTES) // Session expires in 10 minutes

        // 5. Crear el evento y guardarlo en la tabla outbox EN LA MISMA TRANSACCIÓN
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

        // 6. La respuesta al cliente incluye la URL del anuncio
        return RedemptionResult(
            redemptionId = redemption.id,
            status = "PENDING_AD_VIEW",
            adUrl = adCreative.adUrl,
            campaignId = adCreative.campaignId,
            creativeId = adCreative.creativeId
        )
    }
    
    @Transactional
    fun confirmAdWatched(request: ConfirmAdRequest): ConfirmAdResponse {
        val sessionId = request.sessionId
        val sessionData = redisTemplate.opsForHash<String, String>().entries(sessionId)

        val userId = sessionData["userId"] ?: throw IllegalStateException("Session data not found for $sessionId")
        val campaignId = sessionData["campaignId"]?.toLong() ?: throw IllegalStateException("Campaign ID not found in session for $sessionId")
        val creativeId = sessionData["creativeId"] ?: throw IllegalStateException("Creative ID not found in session for $sessionId")

        // 1. Registrar la impresión del anuncio en ad-engine
        adEngineClient.recordImpression(userId.toLong(), campaignId, creativeId)

        // 2. Proceder con la lógica de acreditación de puntos (ej. marcar nonce como usado, acreditar puntos)
        // TODO: Implement actual point accreditation logic
        println("Ad watched confirmed for session: $sessionId. User: $userId, Campaign: $campaignId")

        // Clean up session data from Redis
        redisTemplate.delete(sessionId)

        return ConfirmAdResponse(balance = 100) // Mock balance
    }
}
