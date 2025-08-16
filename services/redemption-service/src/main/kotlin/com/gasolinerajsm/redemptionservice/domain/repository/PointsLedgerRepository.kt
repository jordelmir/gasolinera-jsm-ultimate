package com.gasolinerajsm.redemptionservice.domain.repository

import com.gasolinerajsm.redemptionservice.domain.model.PointsLedgerEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PointsLedgerRepository : JpaRepository<PointsLedgerEntry, UUID>
