package com.gasolinerajsm.adengine.service

import com.gasolinerajsm.adengine.dto.AdCreativeResponse
import com.gasolinerajsm.adengine.dto.AdSelectionRequest
import com.gasolinerajsm.adengine.repository.CampaignRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class AdSelectionService(
    private val campaignRepository: CampaignRepository,
    @Value("\${ad.fallback.url}")
    private val fallbackAdUrl: String
) {

    fun selectAd(request: AdSelectionRequest): AdCreativeResponse {
        val activeCampaigns = campaignRepository.findActiveCampaignsForStation(
            request.stationId,
            Date() // Use current date for selection
        )

        return if (activeCampaigns.isNotEmpty()) {
            val selectedCampaign = activeCampaigns.first() // Simple selection logic
            AdCreativeResponse(
                adUrl = selectedCampaign.adUrl,
                campaignId = selectedCampaign.id,
                creativeId = "creative-${selectedCampaign.id}" // Placeholder creative ID
            )
        } else {
            AdCreativeResponse(
                adUrl = fallbackAdUrl,
                campaignId = -1L,
                creativeId = "fallback-creative"
            )
        }
    }
}