package com.gasolinerajsm.redemptionservice.application

import jakarta.validation.constraints.NotBlank

data class RedeemCommand(
    @field:NotBlank(message = "User ID cannot be blank")
    val userId: String,

    @field:NotBlank(message = "QR token cannot be blank")
    val qrToken: String
)