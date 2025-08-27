package com.gasolinerajsm.stationservice.dto

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StationDtoValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `CreateStationDto should pass validation with valid data`() {
        val dto = CreateStationDto(
            name = "Test Station",
            description = "A test station",
            latitude = 10.123456,
            longitude = -84.123456,
            address = "123 Test Street",
            phone = "+506-1234-5678",
            status = "ACTIVE"
        )

        val violations = validator.validate(dto)
        assertTrue(violations.isEmpty(), "Valid DTO should not have validation errors")
    }

    @Test
    fun `CreateStationDto should fail validation with blank name`() {
        val dto = CreateStationDto(
            name = "",
            latitude = 10.0,
            longitude = -84.0
        )

        val violations = validator.validate(dto)
        assertTrue(violations.isNotEmpty())

        val nameViolations = violations.filter { it.propertyPath.toString() == "name" }
        assertTrue(nameViolations.isNotEmpty())
        assertTrue(nameViolations.any { it.message.contains("required") || it.message.contains("blank") })
    }

    @Test
    fun `CreateStationDto should fail validation with name too long`() {
        val dto = CreateStationDto(
            name = "A".repeat(101), // Exceeds 100 character limit
            latitude = 10.0,
            longitude = -84.0
        )

        val violations = validator.validate(dto)
        val nameViolations = violations.filter { it.propertyPath.toString() == "name" }
        assertTrue(nameViolations.any { it.message.contains("100 characters") })
    }

    @Test
    fun `CreateStationDto should fail validation with invalid latitude`() {
        val invalidLatitudes = listOf(-91.0, 91.0, 100.0, -100.0)

        invalidLatitudes.forEach { lat ->
            val dto = CreateStationDto(
                name = "Test Station",
                latitude = lat,
                longitude = -84.0
            )

            val violations = validator.validate(dto)
            val latViolations = violations.filter { it.propertyPath.toString() == "latitude" }
            assertTrue(latViolations.isNotEmpty(), "Latitude $lat should be invalid")
            assertTrue(latViolations.any { it.message.contains("-90 and 90") })
        }
    }

    @Test
    fun `CreateStationDto should fail validation with invalid longitude`() {
        val invalidLongitudes = listOf(-181.0, 181.0, 200.0, -200.0)

        invalidLongitudes.forEach { lng ->
            val dto = CreateStationDto(
                name = "Test Station",
                latitude = 10.0,
                longitude = lng
            )

            val violations = validator.validate(dto)
            val lngViolations = violations.filter { it.propertyPath.toString() == "longitude" }
            assertTrue(lngViolations.isNotEmpty(), "Longitude $lng should be invalid")
            assertTrue(lngViolations.any { it.message.contains("-180 and 180") })
        }
    }

    @Test
    fun `CreateStationDto should fail validation with description too long`() {
        val dto = CreateStationDto(
            name = "Test Station",
            description = "A".repeat(501), // Exceeds 500 character limit
            latitude = 10.0,
            longitude = -84.0
        )

        val violations = validator.validate(dto)
        val descViolations = violations.filter { it.propertyPath.toString() == "description" }
        assertTrue(descViolations.any { it.message.contains("500 characters") })
    }

    @Test
    fun `CreateStationDto should fail validation with address too long`() {
        val dto = CreateStationDto(
            name = "Test Station",
            latitude = 10.0,
            longitude = -84.0,
            address = "A".repeat(201) // Exceeds 200 character limit
        )

        val violations = validator.validate(dto)
        val addressViolations = violations.filter { it.propertyPath.toString() == "address" }
        assertTrue(addressViolations.any { it.message.contains("200 characters") })
    }

    @Test
    fun `CreateStationDto should fail validation with invalid phone format`() {
        val invalidPhones = listOf(
            "invalid-phone-with-letters",
            "123-456-7890-extra-long-phone-number",
            "phone@domain.com",
            "123#456#7890"
        )

        invalidPhones.forEach { phone ->
            val dto = CreateStationDto(
                name = "Test Station",
                latitude = 10.0,
                longitude = -84.0,
                phone = phone
            )

            val violations = validator.validate(dto)
            val phoneViolations = violations.filter { it.propertyPath.toString() == "phone" }
            assertTrue(phoneViolations.isNotEmpty(), "Phone $phone should be invalid")
        }
    }

    @Test
    fun `CreateStationDto should pass validation with valid phone formats`() {
        val validPhones = listOf(
            "+506-1234-5678",
            "1234567890",
            "+1 (555) 123-4567",
            "506 1234 5678",
            "+506 1234-5678"
        )

        validPhones.forEach { phone ->
            val dto = CreateStationDto(
                name = "Test Station",
                latitude = 10.0,
                longitude = -84.0,
                phone = phone
            )

            val violations = validator.validate(dto)
            val phoneViolations = violations.filter { it.propertyPath.toString() == "phone" }
            assertTrue(phoneViolations.isEmpty(), "Phone $phone should be valid")
        }
    }

    @Test
    fun `CreateStationDto should fail validation with invalid status`() {
        val dto = CreateStationDto(
            name = "Test Station",
            latitude = 10.0,
            longitude = -84.0,
            status = "INVALID_STATUS"
        )

        val violations = validator.validate(dto)
        val statusViolations = violations.filter { it.propertyPath.toString() == "status" }
        assertTrue(statusViolations.any { it.message.contains("ACTIVE, INACTIVE, MAINTENANCE") })
    }

    @Test
    fun `CreateStationDto should pass validation with valid statuses`() {
        val validStatuses = listOf("ACTIVE", "INACTIVE", "MAINTENANCE", "SUSPENDED", "PENDING", "CLOSED")

        validStatuses.forEach { status ->
            val dto = CreateStationDto(
                name = "Test Station",
                latitude = 10.0,
                longitude = -84.0,
                status = status
            )

            val violations = validator.validate(dto)
            val statusViolations = violations.filter { it.propertyPath.toString() == "status" }
            assertTrue(statusViolations.isEmpty(), "Status $status should be valid")
        }
    }

    @Test
    fun `UpdateStationDto should pass validation with all null values`() {
        val dto = UpdateStationDto()

        val violations = validator.validate(dto)
        assertTrue(violations.isEmpty(), "UpdateStationDto with all null values should be valid")
    }

    @Test
    fun `UpdateStationDto should fail validation with invalid values when provided`() {
        val dto = UpdateStationDto(
            name = "", // Invalid: blank
            latitude = 95.0, // Invalid: out of range
            longitude = -185.0, // Invalid: out of range
            description = "A".repeat(501), // Invalid: too long
            address = "A".repeat(201), // Invalid: too long
            phone = "invalid-phone-format",
            status = "INVALID_STATUS"
        )

        val violations = validator.validate(dto)
        assertTrue(violations.size >= 6, "Should have multiple validation errors")

        // Check specific violations
        assertTrue(violations.any { it.propertyPath.toString() == "name" })
        assertTrue(violations.any { it.propertyPath.toString() == "latitude" })
        assertTrue(violations.any { it.propertyPath.toString() == "longitude" })
        assertTrue(violations.any { it.propertyPath.toString() == "description" })
        assertTrue(violations.any { it.propertyPath.toString() == "address" })
        assertTrue(violations.any { it.propertyPath.toString() == "phone" })
        assertTrue(violations.any { it.propertyPath.toString() == "status" })
    }

    @Test
    fun `UpdateStationDto should pass validation with valid partial updates`() {
        val dto = UpdateStationDto(
            name = "Updated Station",
            latitude = 9.123456,
            status = "MAINTENANCE"
        )

        val violations = validator.validate(dto)
        assertTrue(violations.isEmpty(), "Valid partial update should not have validation errors")
    }

    @Test
    fun `should validate coordinate precision correctly`() {
        val dto = CreateStationDto(
            name = "Test Station",
            latitude = 10.123456789, // 9 decimal places
            longitude = -84.123456789 // 9 decimal places
        )

        val violations = validator.validate(dto)
        val coordViolations = violations.filter {
            it.propertyPath.toString() in listOf("latitude", "longitude") &&
            it.message.contains("decimal places")
        }

        // Should allow up to 8 decimal places
        assertTrue(coordViolations.isEmpty(), "Should allow 8 decimal places for coordinates")
    }

    private fun <T> Set<ConstraintViolation<T>>.getViolationMessage(property: String): String? {
        return this.find { it.propertyPath.toString() == property }?.message
    }
}