package com.gasolinerajsm.stationservice.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

class StationPropertiesTest {

    @Test
    fun `should use default values when no configuration provided`() {
        val properties = StationProperties()

        assertEquals("stn_", properties.idPrefix)
        assertEquals(IdGenerationStrategy.UUID, properties.idGenerationStrategy)
        assertEquals(StationStatus.ACTIVE, properties.defaultStatus)

        // Validation defaults
        assertEquals(1, properties.validation.nameMinLength)
        assertEquals(100, properties.validation.nameMaxLength)
        assertEquals(500, properties.validation.descriptionMaxLength)
        assertEquals(-90.0, properties.validation.latitudeMin)
        assertEquals(90.0, properties.validation.latitudeMax)
        assertEquals(-180.0, properties.validation.longitudeMin)
        assertEquals(180.0, properties.validation.longitudeMax)

        // Business rule defaults
        assertFalse(properties.business.allowDuplicateNames)
        assertFalse(properties.business.allowDuplicateLocations)
        assertTrue(properties.business.requireUniqueCoordinates)
    }

    @Test
    fun `should bind configuration properties correctly`() {
        val configMap = mapOf(
            "app.station.id-prefix" to "station_",
            "app.station.id-generation-strategy" to "SEQUENTIAL",
            "app.station.default-status" to "INACTIVE",
            "app.station.validation.name-min-length" to "2",
            "app.station.validation.name-max-length" to "150",
            "app.station.validation.latitude-min" to "-85.0",
            "app.station.validation.latitude-max" to "85.0",
            "app.station.business.allow-duplicate-names" to "true",
            "app.station.business.require-unique-coordinates" to "false"
        )

        val source = MapConfigurationPropertySource(configMap)
        val binder = Binder(source)
        val properties = binder.bind("app.station", StationProperties::class.java).get()

        assertEquals("station_", properties.idPrefix)
        assertEquals(IdGenerationStrategy.SEQUENTIAL, properties.idGenerationStrategy)
        assertEquals(StationStatus.INACTIVE, properties.defaultStatus)
        assertEquals(2, properties.validation.nameMinLength)
        assertEquals(150, properties.validation.nameMaxLength)
        assertEquals(-85.0, properties.validation.latitudeMin)
        assertEquals(85.0, properties.validation.latitudeMax)
        assertTrue(properties.business.allowDuplicateNames)
        assertFalse(properties.business.requireUniqueCoordinates)
    }

    @Test
    fun `should handle partial configuration with defaults`() {
        val configMap = mapOf(
            "app.station.id-prefix" to "custom_",
            "app.station.validation.name-max-length" to "200"
        )

        val source = MapConfigurationPropertySource(configMap)
        val binder = Binder(source)
        val properties = binder.bind("app.station", StationProperties::class.java).get()

        // Configured values
        assertEquals("custom_", properties.idPrefix)
        assertEquals(200, properties.validation.nameMaxLength)

        // Default values
        assertEquals(IdGenerationStrategy.UUID, properties.idGenerationStrategy)
        assertEquals(StationStatus.ACTIVE, properties.defaultStatus)
        assertEquals(1, properties.validation.nameMinLength)
        assertEquals(500, properties.validation.descriptionMaxLength)
    }

    @Test
    fun `StationStatus should provide correct display names`() {
        assertEquals("Activa", StationStatus.ACTIVE.displayName)
        assertEquals("Inactiva", StationStatus.INACTIVE.displayName)
        assertEquals("Mantenimiento", StationStatus.MAINTENANCE.displayName)
        assertEquals("Suspendida", StationStatus.SUSPENDED.displayName)
        assertEquals("Pendiente", StationStatus.PENDING.displayName)
        assertEquals("Cerrada", StationStatus.CLOSED.displayName)
    }

    @Test
    fun `StationStatus should correctly identify active statuses`() {
        assertTrue(StationStatus.ACTIVE.isActive)
        assertFalse(StationStatus.INACTIVE.isActive)
        assertFalse(StationStatus.MAINTENANCE.isActive)
        assertFalse(StationStatus.SUSPENDED.isActive)
        assertFalse(StationStatus.PENDING.isActive)
        assertFalse(StationStatus.CLOSED.isActive)
    }

    @Test
    fun `StationStatus should find status by display name`() {
        assertEquals(StationStatus.ACTIVE, StationStatus.fromDisplayName("Activa"))
        assertEquals(StationStatus.INACTIVE, StationStatus.fromDisplayName("Inactiva"))
        assertEquals(StationStatus.MAINTENANCE, StationStatus.fromDisplayName("Mantenimiento"))

        // Case insensitive
        assertEquals(StationStatus.ACTIVE, StationStatus.fromDisplayName("ACTIVA"))
        assertEquals(StationStatus.ACTIVE, StationStatus.fromDisplayName("activa"))

        // Not found
        assertNull(StationStatus.fromDisplayName("Unknown"))
        assertNull(StationStatus.fromDisplayName(""))
    }

    @Test
    fun `StationStatus should filter active and inactive statuses`() {
        val activeStatuses = StationStatus.getActiveStatuses()
        val inactiveStatuses = StationStatus.getInactiveStatuses()

        assertEquals(1, activeStatuses.size)
        assertTrue(activeStatuses.contains(StationStatus.ACTIVE))

        assertEquals(5, inactiveStatuses.size)
        assertTrue(inactiveStatuses.contains(StationStatus.INACTIVE))
        assertTrue(inactiveStatuses.contains(StationStatus.MAINTENANCE))
        assertTrue(inactiveStatuses.contains(StationStatus.SUSPENDED))
        assertTrue(inactiveStatuses.contains(StationStatus.PENDING))
        assertTrue(inactiveStatuses.contains(StationStatus.CLOSED))

        // Verify no overlap
        assertTrue(activeStatuses.intersect(inactiveStatuses.toSet()).isEmpty())
    }

    @Test
    fun `IdGenerationStrategy should have all expected values`() {
        val strategies = IdGenerationStrategy.values()

        assertEquals(4, strategies.size)
        assertTrue(strategies.contains(IdGenerationStrategy.UUID))
        assertTrue(strategies.contains(IdGenerationStrategy.SEQUENTIAL))
        assertTrue(strategies.contains(IdGenerationStrategy.CUSTOM))
        assertTrue(strategies.contains(IdGenerationStrategy.TIMESTAMP))
    }

    @Test
    fun `ValidationProperties should have reasonable defaults`() {
        val validation = ValidationProperties()

        // Name validation
        assertTrue(validation.nameMinLength > 0)
        assertTrue(validation.nameMaxLength > validation.nameMinLength)

        // Coordinate validation
        assertEquals(-90.0, validation.latitudeMin)
        assertEquals(90.0, validation.latitudeMax)
        assertEquals(-180.0, validation.longitudeMin)
        assertEquals(180.0, validation.longitudeMax)

        // Precision validation
        assertTrue(validation.coordinatePrecision > 0)
        assertTrue(validation.coordinatePrecision < 1.0)
    }

    @Test
    fun `BusinessRuleProperties should have secure defaults`() {
        val business = BusinessRuleProperties()

        // Should prevent duplicates by default
        assertFalse(business.allowDuplicateNames)
        assertFalse(business.allowDuplicateLocations)
        assertTrue(business.requireUniqueCoordinates)

        // Should have reasonable distance requirements
        assertTrue(business.minimumDistanceBetweenStations > 0)

        // Should enable validation by default
        assertTrue(business.enableLocationValidation)
    }

    @Test
    fun `should maintain backward compatibility`() {
        // Test that existing configuration without new properties still works
        val configMap = mapOf(
            "app.station.id-prefix" to "old_",
            "app.station.default-status" to "ACTIVE"
        )

        val source = MapConfigurationPropertySource(configMap)
        val binder = Binder(source)
        val properties = binder.bind("app.station", StationProperties::class.java).get()

        // Original properties should work
        assertEquals("old_", properties.idPrefix)
        assertEquals(StationStatus.ACTIVE, properties.defaultStatus)

        // New properties should use defaults
        assertEquals(IdGenerationStrategy.UUID, properties.idGenerationStrategy)
        assertNotNull(properties.validation)
        assertNotNull(properties.business)
    }
}