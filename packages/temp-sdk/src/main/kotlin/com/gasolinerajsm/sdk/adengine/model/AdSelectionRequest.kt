package com.gasolinerajsm.sdk.adengine.model

data class AdSelectionRequest(
    val user_id: String,
    val station_id: String,
    val context: Map<String, Any>
)