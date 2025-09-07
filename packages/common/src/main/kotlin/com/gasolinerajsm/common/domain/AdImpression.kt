package com.gasolinerajsm.common.domain

import java.time.LocalDateTime
import java.util.UUID

data class AdImpression(
    val id: UUID,
    val creativeId: UUID,
    val userId: String,
    val impressionTime: LocalDateTime
)
