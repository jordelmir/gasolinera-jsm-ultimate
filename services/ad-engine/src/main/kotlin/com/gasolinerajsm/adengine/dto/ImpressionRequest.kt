package com.gasolinerajsm.adengine.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class ImpressionRequest(
    @field:NotBlank(message = "User ID cannot be blank")
    val userId: String,

    @field:NotNull(message = "Campaign ID cannot be null")
    @field:Positive(message = "Campaign ID must be positive")
    val campaignId: Long,

    @field:NotBlank(message = "Creative ID cannot be blank")
    val creativeId: String
)
