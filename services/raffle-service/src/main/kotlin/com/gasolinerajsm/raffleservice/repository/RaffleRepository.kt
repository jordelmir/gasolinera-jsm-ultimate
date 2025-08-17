package com.gasolinerajsm.raffleservice.repository

import com.gasolinerajsm.raffleservice.model.Raffle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RaffleRepository : JpaRepository<Raffle, Long> {
    fun findByPeriod(period: String): Raffle?
}
