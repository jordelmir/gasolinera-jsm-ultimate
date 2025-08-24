package com.gasolinerajsm.raffleservice.repository

import com.gasolinerajsm.raffleservice.model.RaffleWinner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Repository interface for RaffleWinner entity operations
 */
@Repository
interface RaffleWinnerRepository : JpaRepository<RaffleWinner, String> {

    /**
     * Find winner by raffle ID
     */
    fun findByRaffleId(raffleId: String): RaffleWinner?

    /**
     * Find all winners for a specific participant (user)
     */
    fun findByParticipantId(participantId: String): List<RaffleWinner>

    /**
     * Find winners by verification status
     */
    fun findByVerified(verified: Boolean): List<RaffleWinner>

    /**
     * Find winners who haven't claimed their prize
     */
    fun findByPrizeClaimedFalse(): List<RaffleWinner>

    /**
     * Find winners who have claimed their prize
     */
    fun findByPrizeClaimedTrue(): List<RaffleWinner>

    /**
     * Find winners selected within date range
     */
    fun findBySelectionDateBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<RaffleWinner>

    /**
     * Check if raffle has a winner
     */
    fun existsByRaffleId(raffleId: String): Boolean

    /**
     * Count total winners
     */
    override fun count(): Long

    /**
     * Count verified winners
     */
    fun countByVerifiedTrue(): Long

    /**
     * Count unclaimed prizes
     */
    fun countByPrizeClaimedFalse(): Long

    /**
     * Find winners with unclaimed prizes older than specified days
     */
    @Query("""
        SELECT w FROM RaffleWinner w
        WHERE w.prizeClaimed = false
        AND w.verified = true
        AND w.selectionDate < :cutoffDate
    """)
    fun findUnclaimedPrizesOlderThan(@Param("cutoffDate") cutoffDate: LocalDateTime): List<RaffleWinner>

    /**
     * Find recent winners (last N days)
     */
    @Query("""
        SELECT w FROM RaffleWinner w
        WHERE w.selectionDate >= :sinceDate
        ORDER BY w.selectionDate DESC
    """)
    fun findRecentWinners(@Param("sinceDate") sinceDate: LocalDateTime): List<RaffleWinner>

    /**
     * Get winner statistics
     */
    @Query("""
        SELECT
            COUNT(w) as totalWinners,
            COUNT(CASE WHEN w.verified = true THEN 1 END) as verifiedWinners,
            COUNT(CASE WHEN w.prizeClaimed = true THEN 1 END) as claimedPrizes
        FROM RaffleWinner w
    """)
    fun getWinnerStatistics(): Map<String, Long>
}