package com.gasolinerajsm.stationservice.controller

import com.gasolinerajsm.stationservice.dto.CreateStationDto
import com.gasolinerajsm.stationservice.dto.StationDto
import com.gasolinerajsm.stationservice.dto.UpdateStationDto
import com.gasolinerajsm.stationservice.service.StationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST Controller for Station operations
 */
@RestController
@RequestMapping("/api/v1/stations")
@CrossOrigin(origins = ["*"]) // Configure properly for production
@Tag(name = "Stations", description = "Gas station management endpoints")
class StationController(private val stationService: StationService) {

    @Operation(
        summary = "Get all stations",
        description = "Retrieves a list of all gas stations"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "List of stations retrieved successfully",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = Array<StationDto>::class)
            )]
        )
    ])
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllStations(): ResponseEntity<List<StationDto>> {
        val stations = stationService.findAll()
        return ResponseEntity.ok(stations)
    }

    @Operation(
        summary = "Get station by ID",
        description = "Retrieves a specific gas station by its ID"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Station found and returned",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = StationDto::class)
            )]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Station not found"
        )
    ])
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getStationById(
        @Parameter(description = "Station ID", required = true)
        @PathVariable id: String
    ): ResponseEntity<StationDto> {
        return try {
            val station = stationService.findById(id)
            ResponseEntity.ok(station)
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(
        summary = "Create new station",
        description = "Creates a new gas station with the provided information"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "201",
            description = "Station created successfully",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = StationDto::class)
            )]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid station data provided"
        )
    ])
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createStation(
        @Parameter(description = "Station creation data", required = true)
        @Valid @RequestBody createStationDto: CreateStationDto
    ): ResponseEntity<StationDto> {
        val createdStation = stationService.create(createStationDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStation)
    }

    @Operation(
        summary = "Update existing station",
        description = "Updates an existing gas station with new information"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Station updated successfully",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = StationDto::class)
            )]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Station not found"
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid update data provided"
        )
    ])
    @PutMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateStation(
        @Parameter(description = "Station ID to update", required = true)
        @PathVariable id: String,
        @Parameter(description = "Station update data", required = true)
        @Valid @RequestBody updateStationDto: UpdateStationDto
    ): ResponseEntity<StationDto> {
        return try {
            val updatedStation = stationService.update(id, updateStationDto)
            ResponseEntity.ok(updatedStation)
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(
        summary = "Delete station",
        description = "Deletes a gas station by its ID"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "204",
            description = "Station deleted successfully"
        ),
        ApiResponse(
            responseCode = "404",
            description = "Station not found"
        )
    ])
    @DeleteMapping("/{id}")
    fun deleteStation(
        @Parameter(description = "Station ID to delete", required = true)
        @PathVariable id: String
    ): ResponseEntity<Void> {
        return try {
            stationService.deleteById(id)
            ResponseEntity.noContent().build()
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }
}