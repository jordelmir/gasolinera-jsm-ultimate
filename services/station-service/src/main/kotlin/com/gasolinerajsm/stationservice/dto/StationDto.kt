package com.gasolinerajsm.stationservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

/**
 * Data Transfer Object for Station information
 */
data class StationDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val status: String
)

/**
 * Request DTO for creating a new station
 */
data class CreateStationDto(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @field:DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    val latitude: Double,

    @field:DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @field:DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    val longitude: Double,

    val status: String = "ACTIVE"
)

/**
 * Request DTO for updating an existing station
 */
data class UpdateStationDto(
    val name: String?,
    val latitude: Double?,
    val longitude: Double?,
    val status: String?
)