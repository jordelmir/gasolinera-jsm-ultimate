package com.gasolinerajsm.raffleservice.domain.repository

import com.gasolinerajsm.raffleservice.domain.model.Raffle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RaffleRepository : JpaRepository<Raffle, UUID>
