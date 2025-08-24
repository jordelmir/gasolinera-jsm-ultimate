package com.gasolinerajsm.redemptionservice.adapter.`in`.web

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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.http.ResponseEntity

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
        return redemptionService.initiateRedemption(command)
    }

    @PostMapping("/confirm-ad")
    fun confirmAd(@Valid @RequestBody request: ConfirmAdRequest): ConfirmAdResponse {
        // In a real app, get userId from JWT token
        return redemptionService.confirmAdWatched(request)
    }

    @GetMapping("/points-redeemed/count")
    fun countPointsRedeemed(): ResponseEntity<Long> {
        val count = redemptionService.countTotalPointsRedeemed()
        return ResponseEntity.ok(count)
    }
}