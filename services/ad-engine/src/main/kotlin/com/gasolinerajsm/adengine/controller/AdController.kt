package com.gasolinerajsm.adengine.controller

import com.gasolinerajsm.adengine.dto.AdCreativeResponse
import com.gasolinerajsm.adengine.dto.AdSelectionRequest
import com.gasolinerajsm.adengine.dto.ImpressionRequest
import com.gasolinerajsm.adengine.model.AdImpression
import com.gasolinerajsm.adengine.repository.AdImpressionRepository
import com.gasolinerajsm.adengine.service.AdSelectionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ad")
class AdController(
    private val adSelectionService: AdSelectionService,
    private val adImpressionRepository: AdImpressionRepository
) {

    @PostMapping("/select")
    fun selectAd(@RequestBody request: AdSelectionRequest): ResponseEntity<AdCreativeResponse> {
        val adCreative = adSelectionService.selectAd(request)
        return ResponseEntity.ok(adCreative)
    }

    @PostMapping("/impression")
    fun recordImpression(@RequestBody request: ImpressionRequest): ResponseEntity<Void> {
        val adImpression = AdImpression(
            userId = request.userId,
            campaignId = request.campaignId,
            creativeId = request.creativeId // Assuming creativeId is part of ImpressionRequest
        )
        adImpressionRepository.save(adImpression)
        return ResponseEntity.ok().build()
    }
}