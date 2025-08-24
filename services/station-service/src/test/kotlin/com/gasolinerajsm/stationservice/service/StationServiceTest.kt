package com.gasolinerajsm.stationservice.service

import com.gasolinerajsm.stationservice.dto.CreateStationDto
import com.gasolinerajsm.stationservice.dto.UpdateStationDto
import com.gasolinerajsm.stationservice.model.Station
import com.gasolinerajsm.stationservice.repository.StationRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StationServiceTest {

    private val stationRepository: StationRepository = mock()
    private val stationService = StationService(stationRepository)

    @Test
    fun `should find station by id successfully`() {
        // Given
        val stationId = "stn_123"
        val station = Station(
            id = stationId,
            name = "Test Station",
            latitude = 9.9281,
            longitude = -84.0907,
            status = "ACTIVE"
        )
        whenever(stationRepository.findById(stationId)).thenReturn(Optional.of(station))

        // When
        val result = stationService.findById(stationId)

        // Then
        assertEquals(stationId, result.id)
        assertEquals("Test Station", result.name)
        assertEquals(9.9281, result.latitude)
        assertEquals(-84.0907, result.longitude)
        assertEquals("ACTIVE", result.status)
    }

    @Test
    fun `should throw exception when station not found`() {
        // Given
        val stationId = "nonexistent"
        whenever(stationRepository.findById(stationId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<RuntimeException> {
            stationService.findById(stationId)
        }
    }

    @Test
    fun `should create station successfully`() {
        // Given
        val createDto = CreateStationDto(
            name = "New Station",
            latitude = 9.9281,
            longitude = -84.0907,
            status = "ACTIVE"
        )
        val savedStation = Station(
            id = "stn_456",
            name = "New Station",
            latitude = 9.9281,
            longitude = -84.0907,
            status = "Activa"
        )
        whenever(stationRepository.save(any<Station>())).thenReturn(savedStation)

        // When
        val result = stationService.create(createDto)

        // Then
        assertNotNull(result.id)
        assertEquals("New Station", result.name)
        assertEquals(9.9281, result.latitude)
        assertEquals(-84.0907, result.longitude)
        verify(stationRepository).save(any<Station>())
    }

    @Test
    fun `should update station successfully`() {
        // Given
        val stationId = "stn_123"
        val existingStation = Station(
            id = stationId,
            name = "Old Name",
            latitude = 9.9281,
            longitude = -84.0907,
            status = "ACTIVE"
        )
        val updateDto = UpdateStationDto(
            name = "Updated Name",
            latitude = null,
            longitude = null,
            status = "INACTIVE"
        )
        val updatedStation = existingStation.copy(name = "Updated Name", status = "INACTIVE")

        whenever(stationRepository.findById(stationId)).thenReturn(Optional.of(existingStation))
        whenever(stationRepository.save(any<Station>())).thenReturn(updatedStation)

        // When
        val result = stationService.update(stationId, updateDto)

        // Then
        assertEquals("Updated Name", result.name)
        assertEquals("INACTIVE", result.status)
        assertEquals(9.9281, result.latitude) // Should remain unchanged
        verify(stationRepository).save(any<Station>())
    }

    @Test
    fun `should delete station successfully`() {
        // Given
        val stationId = "stn_123"
        whenever(stationRepository.existsById(stationId)).thenReturn(true)

        // When
        stationService.deleteById(stationId)

        // Then
        verify(stationRepository).deleteById(stationId)
    }

    @Test
    fun `should throw exception when deleting nonexistent station`() {
        // Given
        val stationId = "nonexistent"
        whenever(stationRepository.existsById(stationId)).thenReturn(false)

        // When & Then
        assertThrows<RuntimeException> {
            stationService.deleteById(stationId)
        }
    }
}