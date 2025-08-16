package com.gasolinerajsm.redemptionservice.application

import java.util.UUID

data class RedemptionResult(
    val redemptionId: UUID,
    val status: String,
    val adUrl: String? = null, // Ad URL for the client to display
    val campaignId: Long? = null, // Campaign ID associated with the ad
    val creativeId: String? = null // Creative ID associated with the ad
)
