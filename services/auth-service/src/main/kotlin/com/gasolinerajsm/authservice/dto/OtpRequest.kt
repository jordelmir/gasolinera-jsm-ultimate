package com.gasolinerajsm.authservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class OtpRequest(
    @field:NotBlank(message = "Phone number cannot be blank")
    @field:Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number format")
    val phone: String
)