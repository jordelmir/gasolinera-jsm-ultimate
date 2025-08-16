
package com.gasolinerajsm.stationservice.service

import com.gasolinerajsm.stationservice.controller.StationDto
import com.gasolinerajsm.stationservice.repository.StationRepository
import org.springframework.stereotype.Service

@Service
class StationService(private val stationRepository: StationRepository) {

    fun findById(id: String): StationDto {
        return stationRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow { RuntimeException("Station not found") } // Replace with proper exception
    }

    fun findAll(): List<StationDto> {
        return stationRepository.findAll().map { it.toDto() }
    }
}
