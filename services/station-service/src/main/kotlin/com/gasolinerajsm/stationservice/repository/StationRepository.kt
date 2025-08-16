
package com.gasolinerajsm.stationservice.repository

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.gasolinerajsm.stationservice.controller.StationDto

@Entity
@Table(name = "stations")
data class Station(
    @Id
    val id: String,
    val name: String,
    val location: String
) {
    fun toDto() = StationDto(id, name, location)
}

@Repository
interface StationRepository : JpaRepository<Station, String>
