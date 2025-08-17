package com.gasolinerajsm.adengine.repository

import com.gasolinerajsm.adengine.model.AdImpression
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdImpressionRepository : JpaRepository<AdImpression, Long> {
    /**
     * Encuentra todas las impresiones asociadas a un ID de campaña específico.
     */
    fun findByCampaignId(campaignId: Long): List<AdImpression>

    /**
     * Cuenta el número de impresiones para un anunciante específico.
     */
    fun countByAdvertiserId(advertiserId: String): Long
}