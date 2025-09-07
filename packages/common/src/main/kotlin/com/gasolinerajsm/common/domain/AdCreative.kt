package com.gasolinerajsm.common.domain

import java.time.LocalDateTime
import java.util.UUID

enum class CreativeType {
    VIDEO,
    IMAGE,
    BANNER
}

data class AdCreative(
    val id: UUID,
    val campaignId: UUID,
    val name: String,
    val creativeType: CreativeType,
    val assetUrl: String,
    val createdAt: LocalDateTime
)
