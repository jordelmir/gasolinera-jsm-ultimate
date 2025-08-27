package com.gasolinerajsm.stationservice.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

/**
 * Data Transfer Object for Station information.
 * Used for returning station data to clients.
 */
data class StationDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val phone: String? = null,
    val status: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

/**
 * Request DTO for creating a new station.
 * Contains comprehensive validation rules for all required and optional fields.
 */
data class CreateStationDto(
    @field:NotBlank(message = "Station name is required")
    @field:Size(min = 1, max = 100, message = "Station name must be between 1 and 100 characters")
    val name: String,

    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null,

    @field:NotNull(message = "Latitude is required")
    @field:DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90 degrees")
    @field:DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees")
    @field:Digits(integer = 2, fraction = 8, message = "Latitude must have at most 2 integer digits and 8 decimal places")
    val latitude: Double,

    @field:NotNull(message = "Longitude is required")
    @field:DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180 degrees")
    @field:DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180 degrees")
    @field:Digits(integer = 3, fraction = 8, message = "Longitude must have at most 3 integer digits and 8 decimal places")
    val longitude: Double,

    @field:Size(max = 200, message = "Address cannot exceed 200 characters")
    val address: String? = null,

    @field:Pattern(
        regexp = "^[+]?[0-9\\s\\-()]{0,20}$",
        message = "Phone number must contain only digits, spaces, hyphens, parentheses, and plus sign, max 20 characters"
    )
    val phone: String? = null,

    @field:Pattern(
        regexp = "^(ACTIVE|INACTIVE|MAINTENANCE|SUSPENDED|PENDING|CLOSED)$",
        message = "Status must be one of: ACTIVE, INACTIVE, MAINTENANCE, SUSPENDED, PENDING, CLOSED"
    )
    val status: String? = null
)

/**
 * Request DTO for updating an existing station.
 * All fields are optional, allowing partial updates.
 */
data class UpdateStationDto(
    @field:Size(min = 1, max = 100, message = "Station name must be between 1 and 100 characters")
    val name: String? = null,

    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null,

    @field:DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90 degrees")
    @field:DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees")
    @field:Digits(integer = 2, fraction = 8, message = "Latitude must have at most 2 integer digits and 8 decimal places")
    val latitude: Double? = null,

    @field:DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180 degrees")
    @field:DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180 degrees")
    @field:Digits(integer = 3, fraction = 8, message = "Longitude must have at most 3 integer digits and 8 decimal places")
    val longitude: Double? = null,

    @field:Size(max = 200, message = "Address cannot exceed 200 characters")
    val address: String? = null,

    @field:Pattern(
        regexp = "^[+]?[0-9\\s\\-()]{0,20}$",
        message = "Phone number must contain only digits, spaces, hyphens, parentheses, and plus sign, max 20 characters"
    )
    val phone: String? = null,

    @field:Pattern(
        regexp = "^(ACTIVE|INACTIVE|MAINTENANCE|SUSPENDED|PENDING|CLOSED)$",
        message = "Status must be one of: ACTIVE, INACTIVE, MAINTENANCE, SUSPENDED, PENDING, CLOSED"
    )
    val status: String? = null
)

/**
 * DTO for station search and filtering operations.
 */
data class StationSearchDto(
    val name: String? = null,
    val status: String? = null,
    val nearLatitude: Double? = null,
    val nearLongitude: Double? = null,
    val radiusKm: Double? = null,
    val page: Int = 0,
    val size: Int = 20
)

/**
 * Response DTO for paginated station results.
 */
data class StationPageDto(
    val content: List<StationDto>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)