package com.gasolinerajsm.redemptionservice.application

import jakarta.validation.constraints.NotBlank

data class ConfirmAdRequest(
    @field:NotBlank(message = "Session ID cannot be blank")
    val sessionId: String
)

data class ConfirmAdResponse(
    val balance: Int
)