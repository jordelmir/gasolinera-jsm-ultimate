
package com.gasolinerajsm.adengine.service

import com.gasolinerajsm.adengine.controller.AdCreative
import com.gasolinerajsm.adengine.controller.AdSelectionRequest
import com.gasolinerajsm.adengine.repository.AdCampaignRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AdSelectionService(
    private val campaignRepository: AdCampaignRepository
) {

    @Value("\${app.admob.fallback_ad_url}")
    private lateinit var fallbackAdUrl: String

    fun selectAd(request: AdSelectionRequest): AdCreative {
        val activeCampaigns = campaignRepository.findActiveCampaigns()

        // Simple rule engine: find the first campaign that targets the station
        val matchedCampaign = activeCampaigns.firstOrNull { campaign ->
            val targetStations = campaign.targeting_rules?.get("stations") as? List<String>
            targetStations?.contains(request.station_id) ?: false
        }

        return if (matchedCampaign != null) {
            AdCreative(
                creative_url = matchedCampaign.ad_creative_url ?: fallbackAdUrl,
                impression_url = "/ad/impression/${matchedCampaign.id}",
                duration_seconds = 15
            )
        } else {
            // Fallback to AdMob
            AdCreative(fallbackAdUrl, "/ad/impression/admob", 15)
        }
    }
}
