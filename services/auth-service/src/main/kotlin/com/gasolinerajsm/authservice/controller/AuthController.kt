
package com.gasolinerajsm.authservice.controller

import com.gasolinerajsm.authservice.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/otp/request")
    fun requestOtp(@RequestBody request: OtpRequest) {
        authService.sendOtp(request.phone)
    }

    @PostMapping("/otp/verify")
    fun verifyOtp(@RequestBody request: OtpVerifyRequest): TokenResponse {
        return authService.verifyOtpAndIssueTokens(request.phone, request.code)
    }
}

data class OtpRequest(val phone: String)
data class OtpVerifyRequest(val phone: String, val code: String)
data class TokenResponse(val access_token: String, val refresh_token: String)
