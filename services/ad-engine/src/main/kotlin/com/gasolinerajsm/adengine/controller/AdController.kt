package com.gasolinerajsm.adengine.controller

import com.gasolinerajsm.adengine.dto.AdCreativeResponse
import com.gasolinerajsm.adengine.dto.AdSelectionRequest
import com.gasolinerajsm.adengine.dto.ImpressionRequest
import com.gasolinerajsm.adengine.model.AdImpression
import com.gasolinerajsm.adengine.repository.AdImpressionRepository
import com.gasolinerajsm.adengine.service.AdSelectionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/ad")
class AdController(
    private val adSelectionService: AdSelectionService,
    private val adImpressionRepository: AdImpressionRepository
) {

    private val logger = LoggerFactory.getLogger(AdController::class.java)

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

    @GetMapping("/impressions")
    fun getImpressions( @RequestParam(required = false) campaignId: Long?): ResponseEntity<List<AdImpression>> {
        logger.info("Received request to get impressions. CampaignId filter: {}", campaignId ?: "none")
        val impressions = if (campaignId != null) {
            adImpressionRepository.findByCampaignId(campaignId)
        } else {
            adImpressionRepository.findAll()
        }
        return ResponseEntity.ok(impressions)
    }
}