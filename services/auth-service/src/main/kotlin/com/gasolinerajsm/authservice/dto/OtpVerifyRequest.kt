package com.gasolinerajsm.authservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class OtpVerifyRequest(
    @field:NotBlank(message = "Phone number cannot be blank")
    @field:Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number format")
    val phone: String,

    @field:NotBlank(message = "OTP code cannot be blank")
    @field:Size(min = 6, max = 6, message = "OTP code must be 6 digits")
    val code: String
)