
package com.gasolinerajsm.redemptionservice.controller

import com.gasolinerajsm.redemptionservice.service.RedemptionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/redeem")
class RedemptionController(private val redemptionService: RedemptionService) {

    @PostMapping
    fun redeem(@RequestBody request: RedeemRequest): RedeemResponse {
        // In a real app, get userId from JWT token (SecurityContextHolder)
        val userId = "user-placeholder-id" 
        return redemptionService.initiateRedemption(userId, request)
    }

    @PostMapping("/confirm-ad")
    fun confirmAd(@RequestBody request: ConfirmAdRequest): ConfirmAdResponse {
        // In a real app, get userId from JWT token
        val userId = "user-placeholder-id"
        return redemptionService.confirmAdWatched(userId, request)
    }
}

data class RedeemRequest(val qr_token: String, val gps: String)
data class RedeemResponse(val session_id: String, val ad_payload: Any, val provisional_points: Int)
data class ConfirmAdRequest(val session_id: String)
data class ConfirmAdResponse(val balance: Int)
