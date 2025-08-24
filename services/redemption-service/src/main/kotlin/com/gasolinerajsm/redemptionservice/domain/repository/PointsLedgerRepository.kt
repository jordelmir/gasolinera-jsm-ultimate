package com.gasolinerajsm.redemptionservice.domain.repository

import com.gasolinerajsm.redemptionservice.domain.model.PointsLedgerEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PointsLedgerRepository : JpaRepository<PointsLedgerEntry, UUID> {

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(p.points), 0) FROM PointsLedgerEntry p WHERE p.type = 'CREDIT'")
    fun sumPointsCredited(): Long
}
