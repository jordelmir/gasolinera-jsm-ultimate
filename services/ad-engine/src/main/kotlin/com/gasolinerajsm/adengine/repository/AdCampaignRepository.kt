
package com.gasolinerajsm.adengine.repository

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Entity
@Table(name = "ad_campaigns")
data class AdCampaign(
    @Id
    val id: java.util.UUID = java.util.UUID.randomUUID(),
    val advertiser_id: java.util.UUID,
    val name: String,
    val ad_creative_url: String?,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val targeting_rules: Map<String, Any>? = null,
    val active: Boolean = true
)

@Repository
interface AdCampaignRepository : JpaRepository<AdCampaign, java.util.UUID> {
    @Query("SELECT c FROM AdCampaign c WHERE c.active = true AND (c.start_date IS NULL OR c.start_date <= CURRENT_DATE) AND (c.end_date IS NULL OR c.end_date >= CURRENT_DATE)")
    fun findActiveCampaigns(): List<AdCampaign>
}
