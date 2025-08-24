package com.gasolinerajsm.raffleservice.repository

import com.gasolinerajsm.raffleservice.model.RaffleParticipant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Repository interface for RaffleParticipant entity operations
 */
@Repository
interface RaffleParticipantRepository : JpaRepository<RaffleParticipant, String> {

    /**
     * Find all participants for a specific raffle
     */
    fun findByRaffleId(raffleId: String): List<RaffleParticipant>

    /**
     * Find all raffles a user has participated in
     */
    fun findByUserId(userId: String): List<RaffleParticipant>

    /**
     * Check if user is already participating in a raffle
     */
    fun existsByRaffleIdAndUserId(raffleId: String, userId: String): Boolean

    /**
     * Find participant by raffle and user
     */
    fun findByRaffleIdAndUserId(raffleId: String, userId: String): RaffleParticipant?

    /**
     * Count participants in a raffle
     */
    fun countByRaffleId(raffleId: String): Long

    /**
     * Find participants by raffle ordered by participation date
     */
    fun findByRaffleIdOrderByParticipationDateAsc(raffleId: String): List<RaffleParticipant>

    /**
     * Find participants within date range
     */
    fun findByParticipationDateBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<RaffleParticipant>

    /**
     * Get participant entry hashes for Merkle Tree generation
     */
    @Query("""
        SELECT p.entryHash FROM RaffleParticipant p
        WHERE p.raffleId = :raffleId
        ORDER BY p.participationDate ASC
    """)
    fun findEntryHashesByRaffleId(@Param("raffleId") raffleId: String): List<String>

    /**
     * Find participants with their entry strings for verification
     */
    @Query("""
        SELECT p FROM RaffleParticipant p
        WHERE p.raffleId = :raffleId
        ORDER BY p.participationDate ASC
    """)
    fun findParticipantsForMerkleTree(@Param("raffleId") raffleId: String): List<RaffleParticipant>

    /**
     * Count total participants across all raffles for a user
     */
    @Query("""
        SELECT COUNT(p) FROM RaffleParticipant p
        WHERE p.userId = :userId
    """)
    fun countTotalParticipationsByUser(@Param("userId") userId: String): Long
}