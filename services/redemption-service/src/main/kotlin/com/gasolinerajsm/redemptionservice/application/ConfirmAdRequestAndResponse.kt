package com.gasolinerajsm.redemptionservice.application

data class ConfirmAdRequest(val sessionId: String)
data class ConfirmAdResponse(val balance: Int)
