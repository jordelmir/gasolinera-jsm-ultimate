package com.gasolinerajsm.stationservice.repository

import com.gasolinerajsm.stationservice.model.Station
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository interface for Station entity operations
 */
@Repository
interface StationRepository : JpaRepository<Station, String> {

    /**
     * Find stations by status
     */
    fun findByStatus(status: String): List<Station>

    /**
     * Find stations by name containing (case insensitive)
     */
    fun findByNameContainingIgnoreCase(name: String): List<Station>

    /**
     * Find stations within a geographic radius
     */
    @Query("""
        SELECT s FROM Station s
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude)) *
               cos(radians(s.longitude) - radians(:lng)) +
               sin(radians(:lat)) * sin(radians(s.latitude)))) <= :radius
    """)
    fun findStationsWithinRadius(
        @Param("lat") latitude: Double,
        @Param("lng") longitude: Double,
        @Param("radius") radiusKm: Double
    ): List<Station>

    /**
     * Count active stations
     */
    fun countByStatus(status: String): Long
}