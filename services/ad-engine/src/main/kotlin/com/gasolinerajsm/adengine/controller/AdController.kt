
package com.gasolinerajsm.adengine.controller

import com.gasolinerajsm.adengine.service.AdSelectionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ad")
class AdController(private val adSelectionService: AdSelectionService) {

    @PostMapping("/select")
    fun selectAd(@RequestBody request: AdSelectionRequest): AdCreative {
        return adSelectionService.selectAd(request)
    }
}

data class AdSelectionRequest(val user_id: String, val station_id: String, val context: Map<String, Any>)
data class AdCreative(val creative_url: String, val impression_url: String, val duration_seconds: Int)
