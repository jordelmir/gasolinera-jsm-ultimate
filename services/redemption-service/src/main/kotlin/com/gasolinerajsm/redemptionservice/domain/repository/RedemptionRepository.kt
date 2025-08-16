package com.gasolinerajsm.redemptionservice.domain.repository

import com.gasolinerajsm.redemptionservice.domain.aggregate.Redemption

interface RedemptionRepository {
    fun save(redemption: Redemption): Redemption
    fun findById(id: String): Redemption?
}
