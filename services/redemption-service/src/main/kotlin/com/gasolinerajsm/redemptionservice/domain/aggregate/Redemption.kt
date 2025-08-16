package com.gasolinerajsm.redemptionservice.domain.aggregate

import com.gasolinerajsm.redemptionservice.service.QrSecurityService.QrPayload
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "redemptions") // Assuming a table named 'redemptions'
data class Redemption(
    @Id
    val id: UUID = UUID.randomUUID(),
    val userId: String,
    val stationId: String,
    val dispenserId: String,
    val nonce: String,
    val timestamp: Long,
    val expiration: Long
) {
    companion object {
        fun initiate(userId: String, qr: QrPayload): Redemption {
            return Redemption(
                userId = userId,
                stationId = qr.s,
                dispenserId = qr.d,
                nonce = qr.n,
                timestamp = qr.t,
                expiration = qr.exp
            )
        }
    }
}
