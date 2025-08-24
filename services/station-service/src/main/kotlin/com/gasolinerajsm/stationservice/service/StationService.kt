package com.gasolinerajsm.stationservice.service

import com.gasolinerajsm.stationservice.dto.CreateStationDto
import com.gasolinerajsm.stationservice.dto.StationDto
import com.gasolinerajsm.stationservice.dto.UpdateStationDto
import com.gasolinerajsm.stationservice.model.Station
import com.gasolinerajsm.stationservice.repository.StationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class StationService(private val stationRepository: StationRepository) {

    fun findById(id: String): StationDto {
        return stationRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow { RuntimeException("Station with id $id not found") } // TODO: Use custom exception
    }

    fun findAll(): List<StationDto> {
        return stationRepository.findAll().map { it.toDto() }
    }

    @Transactional
    fun create(stationDto: CreateStationDto): StationDto {
        val newStation = Station(
            id = "stn_" + UUID.randomUUID().toString(),
            name = stationDto.name,
            latitude = stationDto.latitude,
            longitude = stationDto.longitude,
            status = "Activa" // Default status
        )
        val savedStation = stationRepository.save(newStation)
        return savedStation.toDto()
    }

    @Transactional
    fun update(id: String, stationDto: UpdateStationDto): StationDto {
        val existingStation = stationRepository.findById(id)
            .orElseThrow { RuntimeException("Station with id $id not found") }

        existingStation.name = stationDto.name ?: existingStation.name
        existingStation.latitude = stationDto.latitude ?: existingStation.latitude
        existingStation.longitude = stationDto.longitude ?: existingStation.longitude
        existingStation.status = stationDto.status ?: existingStation.status

        val updatedStation = stationRepository.save(existingStation)
        return updatedStation.toDto()
    }

    @Transactional
    fun deleteById(id: String) {
        if (!stationRepository.existsById(id)) {
            throw RuntimeException("Station with id $id not found")
        }
        stationRepository.deleteById(id)
    }
}

// Helper extension function to map Entity to DTO
fun Station.toDto(): StationDto = StationDto(
    id = this.id,
    name = this.name,
    latitude = this.latitude,
    longitude = this.longitude,
    status = this.status
)