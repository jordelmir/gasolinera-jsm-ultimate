package com.gasolinerajsm.redemptionservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.gasolinerajsm.redemptionservice.exception.InvalidQrException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64

data class QrPayload(
    val s: String, // stationId
    val d: String, // dispenserId
    val n: String, // nonce
    val t: Long,   // timestamp
    val exp: Long  // expiration
)

@Service
class QrSecurityService(
    @Value("\${qr.public.key}")
    private val qrPublicKeyPem: String,
    private val objectMapper: ObjectMapper // Inject ObjectMapper
) {

    private val publicKey: PublicKey by lazy {
        val publicKeyPEM = qrPublicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
        val encoded = Base64.getDecoder().decode(publicKeyPEM)
        val keySpec = X509EncodedKeySpec(encoded)
        KeyFactory.getInstance("EC").generatePublic(keySpec)
    }

    fun validateAndParseToken(token: String): QrPayload {
        val parts = token.split(".")
        if (parts.size != 2) {
            throw InvalidQrException("Invalid QR token format")
        }

        val (encodedPayload, receivedSignatureBase64Url) = parts

        val decodedPayloadBytes = try {
            Base64.getUrlDecoder().decode(encodedPayload)
        } catch (e: IllegalArgumentException) {
            throw InvalidQrException("Invalid base64url encoding for payload")
        }

        val decodedReceivedSignatureBytes = try {
            Base64.getUrlDecoder().decode(receivedSignatureBase64Url)
        } catch (e: IllegalArgumentException) {
            throw InvalidQrException("Invalid base64url encoding for signature")
        }

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initVerify(publicKey)
        signature.update(decodedPayloadBytes)

        if (!signature.verify(decodedReceivedSignatureBytes)) {
            throw InvalidQrException("Invalid QR signature")
        }

        val payloadJson = String(decodedPayloadBytes, Charsets.UTF_8)
        val qrPayload = try {
            objectMapper.readValue(payloadJson, QrPayload::class.java)
        } catch (e: Exception) {
            throw InvalidQrException("Invalid QR payload JSON: ${e.message}")
        }

        if (qrPayload.exp < Instant.now().epochSecond) {
            throw InvalidQrException("QR token has expired")
        }

        return qrPayload
    }
}
