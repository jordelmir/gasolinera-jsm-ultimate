package com.gasolinerajsm.stationservice.exception

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StationExceptionsTest {

    @Test
    fun `StationNotFoundException should contain station ID in message`() {
        val stationId = "stn_123"
        val exception = StationNotFoundException(stationId)

        assertEquals("Station not found with ID: $stationId", exception.message)
        assertTrue(exception is StationServiceException)
        assertNull(exception.cause)
    }

    @Test
    fun `StationAlreadyExistsException should contain identifier in message`() {
        val identifier = "Station ABC"
        val exception = StationAlreadyExistsException(identifier)

        assertEquals("Station already exists with identifier: $identifier", exception.message)
        assertTrue(exception is StationServiceException)
    }

    @Test
    fun `StationAlreadyExistsException should include details when provided`() {
        val identifier = "Station ABC"
        val details = "at coordinates (10.0, 20.0)"
        val exception = StationAlreadyExistsException(identifier, details)

        assertEquals("Station already exists with identifier: $identifier ($details)", exception.message)
    }

    @Test
    fun `StationValidationException should contain field and message`() {
        val field = "latitude"
        val message = "must be between -90 and 90"
        val exception = StationValidationException(field, message)

        assertEquals("Validation failed for field '$field': $message", exception.message)
        assertTrue(exception is StationServiceException)
    }

    @Test
    fun `StationValidationException should include value when provided`() {
        val field = "latitude"
        val message = "must be between -90 and 90"
        val value = 95.0
        val exception = StationValidationException(field, message, value)

        assertEquals("Validation failed for field '$field': $message (provided value: $value)", exception.message)
    }

    @Test
    fun `StationBusinessRuleException should contain rule and message`() {
        val rule = "UNIQUE_LOCATION"
        val message = "Station already exists at this location"
        val exception = StationBusinessRuleException(rule, message)

        assertEquals("Business rule violation '$rule': $message", exception.message)
        assertTrue(exception is StationServiceException)
    }

    @Test
    fun `StationDataIntegrityException should contain operation and message`() {
        val operation = "CREATE"
        val message = "Duplicate key violation"
        val exception = StationDataIntegrityException(operation, message)

        assertEquals("Data integrity violation during $operation: $message", exception.message)
        assertTrue(exception is StationServiceException)
    }

    @Test
    fun `StationDataIntegrityException should preserve cause when provided`() {
        val operation = "UPDATE"
        val message = "Foreign key constraint violation"
        val cause = RuntimeException("Database error")
        val exception = StationDataIntegrityException(operation, message, cause)

        assertEquals("Data integrity violation during $operation: $message", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `all exceptions should extend StationServiceException`() {
        val exceptions = listOf(
            StationNotFoundException("test"),
            StationAlreadyExistsException("test"),
            StationValidationException("field", "message"),
            StationBusinessRuleException("rule", "message"),
            StationDataIntegrityException("operation", "message")
        )

        exceptions.forEach { exception ->
            assertTrue(exception is StationServiceException,
                "${exception::class.simpleName} should extend StationServiceException")
        }
    }

    @Test
    fun `all exceptions should extend RuntimeException`() {
        val exceptions = listOf(
            StationNotFoundException("test"),
            StationAlreadyExistsException("test"),
            StationValidationException("field", "message"),
            StationBusinessRuleException("rule", "message"),
            StationDataIntegrityException("operation", "message")
        )

        exceptions.forEach { exception ->
            assertTrue(exception is RuntimeException,
                "${exception::class.simpleName} should extend RuntimeException")
        }
    }

    @Test
    fun `exceptions should have meaningful toString representation`() {
        val exception = StationNotFoundException("stn_123")
        val toString = exception.toString()

        assertTrue(toString.contains("StationNotFoundException"))
        assertTrue(toString.contains("Station not found with ID: stn_123"))
    }
}