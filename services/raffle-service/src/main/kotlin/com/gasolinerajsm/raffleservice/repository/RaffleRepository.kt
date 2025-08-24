package com.gasolinerajsm.raffleservice.repository

import com.gasolinerajsm.raffleservice.model.Raffle
import com.gasolinerajsm.raffleservice.model.RaffleStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Repository interface for Raffle entity operations
 */
@Repository
interface RaffleRepository : JpaRepository<Raffle, String> {

    /**
     * Find raffles by status
     */
    fun findByStatus(status: RaffleStatus): List<Raffle>

    /**
     * Find active raffles (open and within date range)
     */
    @Query("""
        SELECT r FROM Raffle r
        WHERE r.status = 'OPEN'
        AND r.startDate <= :now
        AND r.endDate > :now
    """)
    fun findActiveRaffles(@Param("now") now: LocalDateTime = LocalDateTime.now()): List<Raffle>

    /**
     * Find raffles ready for drawing (closed or past end date)
     */
    @Query("""
        SELECT r FROM Raffle r
        WHERE (r.status = 'OPEN' AND r.endDate <= :now)
        OR r.status = 'CLOSED'
    """)
    fun findRafflesReadyForDraw(@Param("now") now: LocalDateTime = LocalDateTime.now()): List<Raffle>

    /**
     * Find raffles by name containing (case insensitive)
     */
    fun findByNameContainingIgnoreCase(name: String): List<Raffle>

    /**
     * Find raffles created within date range
     */
    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Raffle>

    /**
     * Count raffles by status
     */
    fun countByStatus(status: RaffleStatus): Long

    /**
     * Find raffles ending soon (within specified hours)
     */
    @Query("""
        SELECT r FROM Raffle r
        WHERE r.status = 'OPEN'
        AND r.endDate BETWEEN :now AND :endTime
    """)
    fun findRafflesEndingSoon(
        @Param("now") now: LocalDateTime = LocalDateTime.now(),
        @Param("endTime") endTime: LocalDateTime = LocalDateTime.now().plusHours(24)
    ): List<Raffle>

    /**
     * Check if raffle exists by name and is active
     */
    fun existsByNameAndStatus(name: String, status: RaffleStatus): Boolean
}