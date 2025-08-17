package com.gasolinerajsm.redemptionservice.adapter.in.web

import com.gasolinerajsm.redemptionservice.application.RedemptionService
import com.gasolinerajsm.redemptionservice.service.QrSecurityService
import com.gasolinerajsm.redemptionservice.application.RedeemCommand
import com.gasolinerajsm.redemptionservice.application.RedemptionResult
import com.gasolinerajsm.redemptionservice.application.ConfirmAdRequest
import com.gasolinerajsm.redemptionservice.application.ConfirmAdResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/redeem")
class RedemptionController(
    private val redemptionService: RedemptionService,
    private val qrSecurityService: QrSecurityService
) {

    @PostMapping
    fun redeem(@Valid @RequestBody command: RedeemCommand): RedemptionResult {
        val qrPayload = qrSecurityService.validateAndParseToken(command.qrToken)
        // In a real app, get userId from JWT token (SecurityContextHolder)
        val userId = "user-placeholder-id" 
        return redemptionService.initiateRedemption(userId, command)
    }

    @PostMapping("/confirm-ad")
    fun confirmAd(@Valid @RequestBody request: ConfirmAdRequest): ConfirmAdResponse {
        // In a real app, get userId from JWT token
        val userId = "user-placeholder-id"
        return redemptionService.confirmAdWatched(userId, request)
    }
}