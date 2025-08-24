package com.gasolinerajsm.redemptionservice.adapter.out.persistence

import com.gasolinerajsm.redemptionservice.adapter.out.cdc.Outbox
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OutboxRepository : JpaRepository<Outbox, UUID> {
    // JpaRepository provides save, findById, etc.
}
