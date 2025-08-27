package com.gasolinerajsm.stationservice.exception

/**
 * Base exception for all station service related errors.
 * Provides a common base for exception handling and logging.
 */
abstract class StationServiceException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exception thrown when a requested station is not found.
 * Maps to HTTP 404 Not Found.
 */
class StationNotFoundException(
    stationId: String
) : StationServiceException("Station not found with ID: $stationId")

/**
 * Exception thrown when attempting to create a station that already exists.
 * Maps to HTTP 409 Conflict.
 */
class StationAlreadyExistsException(
    identifier: String,
    details: String? = null
) : StationServiceException(
    "Station already exists with identifier: $identifier" +
    if (details != null) " ($details)" else ""
)

/**
 * Exception thrown when station data validation fails.
 * Maps to HTTP 400 Bad Request.
 */
class StationValidationException(
    field: String,
    message: String,
    value: Any? = null
) : StationServiceException(
    "Validation failed for field '$field': $message" +
    if (value != null) " (provided value: $value)" else ""
)

/**
 * Exception thrown when station business rules are violated.
 * Maps to HTTP 422 Unprocessable Entity.
 */
class StationBusinessRuleException(
    rule: String,
    message: String
) : StationServiceException("Business rule violation '$rule': $message")

/**
 * Exception thrown when station operations fail due to data integrity issues.
 * Maps to HTTP 409 Conflict.
 */
class StationDataIntegrityException(
    operation: String,
    message: String,
    cause: Throwable? = null
) : StationServiceException("Data integrity violation during $operation: $message", cause)