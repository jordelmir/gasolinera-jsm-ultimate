package com.gasolinerajsm.redemptionservice.application

data class RedeemCommand(
    val userId: String,
    val qrToken: String
)
