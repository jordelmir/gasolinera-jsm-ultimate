package com.gasolinerajsm.stationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Configuration properties for station service.
 * Allows external configuration of ID generation, validation rules, and default values.
 */
@Component
@ConfigurationProperties(prefix = "app.station")
data class StationProperties(
    var idPrefix: String = "stn_",
    var idGenerationStrategy: IdGenerationStrategy = IdGenerationStrategy.UUID,
    var defaultStatus: StationStatus = StationStatus.ACTIVE,
    var validation: ValidationProperties = ValidationProperties(),
    var business: BusinessRuleProperties = BusinessRuleProperties()
)

/**
 * Validation configuration properties.
 * Defines limits and constraints for station data validation.
 */
data class ValidationProperties(
    var nameMinLength: Int = 1,
    var nameMaxLength: Int = 100,
    var descriptionMaxLength: Int = 500,
    var addressMaxLength: Int = 200,
    var phoneMaxLength: Int = 20,
    var latitudeMin: Double = -90.0,
    var latitudeMax: Double = 90.0,
    var longitudeMin: Double = -180.0,
    var longitudeMax: Double = 180.0,
    var coordinatePrecision: Double = 0.000001 // Minimum distance between stations in degrees
)

/**
 * Business rule configuration properties.
 * Defines business logic constraints and behaviors.
 */
data class BusinessRuleProperties(
    var allowDuplicateNames: Boolean = false,
    var allowDuplicateLocations: Boolean = false,
    var requireUniqueCoordinates: Boolean = true,
    var minimumDistanceBetweenStations: Double = 0.001, // In degrees (~100 meters)
    var enableLocationValidation: Boolean = true,
    var enableBusinessHoursValidation: Boolean = false
)

/**
 * Station status enumeration with display names.
 * Provides type safety and consistent status values.
 */
enum class StationStatus(val displayName: String, val isActive: Boolean) {
    ACTIVE("Activa", true),
    INACTIVE("Inactiva", false),
    MAINTENANCE("Mantenimiento", false),
    SUSPENDED("Suspendida", false),
    PENDING("Pendiente", false),
    CLOSED("Cerrada", false);

    companion object {
        fun fromDisplayName(displayName: String): StationStatus? {
            return values().find { it.displayName.equals(displayName, ignoreCase = true) }
        }

        fun getActiveStatuses(): List<StationStatus> {
            return values().filter { it.isActive }
        }

        fun getInactiveStatuses(): List<StationStatus> {
            return values().filter { !it.isActive }
        }
    }
}

/**
 * ID generation strategy enumeration.
 * Defines different approaches for generating station IDs.
 */
enum class IdGenerationStrategy {
    /**
     * Use UUID for ID generation (default).
     * Provides globally unique identifiers.
     */
    UUID,

    /**
     * Use sequential numbering for ID generation.
     * Provides predictable, ordered identifiers.
     */
    SEQUENTIAL,

    /**
     * Use custom ID generation logic.
     * Allows for business-specific ID patterns.
     */
    CUSTOM,

    /**
     * Use timestamp-based ID generation.
     * Provides time-ordered identifiers.
     */
    TIMESTAMP
}