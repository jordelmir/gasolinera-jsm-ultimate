package com.gasolinerajsm.redemptionservice.domain.event

import java.util.UUID

data class RedemptionInitiatedEvent(
    val redemptionId: UUID,
    val userId: String,
    val stationId: String
)
