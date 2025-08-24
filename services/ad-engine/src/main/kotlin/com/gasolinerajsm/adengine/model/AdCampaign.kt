package com.gasolinerajsm.adengine.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "ad_campaigns")
data class AdCampaign(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val advertiserId: Long,
    val name: String,
    val adUrl: String,
    val stationId: Long?,
    val startDate: Instant,
    val endDate: Instant,
    val isActive: Boolean,
    val status: String = "ACTIVE",
    val budgetTotal: Double = 0.0,
    val budgetSpent: Double = 0.0,
    @ElementCollection
    val targetStations: Set<String> = emptySet()
)
