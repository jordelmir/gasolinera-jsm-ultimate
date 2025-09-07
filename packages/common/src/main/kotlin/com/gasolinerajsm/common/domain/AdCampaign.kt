package com.gasolinerajsm.common.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class AdCampaign(
    val id: UUID,
    val name: String,
    val advertiserId: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val budget: BigDecimal,
    val status: CampaignStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
