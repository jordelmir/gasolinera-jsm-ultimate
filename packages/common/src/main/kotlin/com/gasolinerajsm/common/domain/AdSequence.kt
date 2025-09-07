package com.gasolinerajsm.common.domain

import java.util.UUID

data class AdSequence(
    val id: UUID,
    val name: String,
    val creativeIds: List<UUID>
)
