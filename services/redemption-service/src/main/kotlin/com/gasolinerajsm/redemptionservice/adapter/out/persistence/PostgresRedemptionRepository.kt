package com.gasolinerajsm.redemptionservice.adapter.out.persistence

import com.gasolinerajsm.redemptionservice.domain.aggregate.Redemption
import com.gasolinerajsm.redemptionservice.domain.repository.RedemptionRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PostgresRedemptionRepository : JpaRepository<Redemption, UUID>, RedemptionRepository {
    // JpaRepository provides save, findById, etc.
    // The methods from RedemptionRepository interface are now implicitly implemented by JpaRepository
    override fun save(redemption: Redemption): Redemption {
        return save(redemption)
    }

    override fun findById(id: String): Redemption? {
        return findById(UUID.fromString(id)).orElse(null)
    }
}
