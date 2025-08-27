# Design Document

## Overview

This design addresses the identified issues in the StationService by implementing custom exception handling, configurable values, comprehensive input validation, optimized database operations, and improved service architecture. The improvements maintain backward compatibility while significantly enhancing code quality, maintainability, and robustness.

## Architecture

### Current Architecture Strengths

- Clean separation with DTOs and entity mapping
- Proper use of Spring Data JPA repositories
- Transactional management
- RESTful controller design

### Enhanced Architecture

The enhanced architecture maintains the existing structure while adding:

1. **Custom Exception Hierarchy**: Specific exceptions for different error scenarios
2. **Configuration Management**: Externalized configuration for IDs, statuses, and validation rules
3. **Input Validation**: Comprehensive DTO validation with custom validators
4. **Optimized Data Access**: Improved database operations following Spring Data JPA best practices
5. **Enhanced Logging**: Structured logging with monitoring integration

## Components and Interfaces

### Custom Exception Hierarchy

```kotlin
// Base exception for station-related errors
abstract class StationServiceException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

// Specific exceptions
class StationNotFoundException(stationId: String) :
    StationServiceException("Station not found with ID: $stationId")

class StationAlreadyExistsException(identifier: String) :
    StationServiceException("Station already exists with identifier: $identifier")

class StationValidationException(field: String, message: String) :
    StationServiceException("Validation failed for field '$field': $message")
```

### Configuration Properties

```kotlin
@ConfigurationProperties(prefix = "app.station")
data class StationProperties(
    var idPrefix: String = "stn_",
    var idGenerationStrategy: IdGenerationStrategy = IdGenerationStrategy.UUID,
    var defaultStatus: StationStatus = StationStatus.ACTIVE,
    var validation: ValidationProperties = ValidationProperties()
)

data class ValidationProperties(
    var nameMinLength: Int = 1,
    var nameMaxLength: Int = 100,
    var descriptionMaxLength: Int = 500,
    var latitudeMin: Double = -90.0,
    var latitudeMax: Double = 90.0,
    var longitudeMin: Double = -180.0,
    var longitudeMax: Double = 180.0
)

enum class StationStatus(val displayName: String) {
    ACTIVE("Activa"),
    INACTIVE("Inactiva"),
    MAINTENANCE("Mantenimiento"),
    SUSPENDED("Suspendida")
}

enum class IdGenerationStrategy {
    UUID, SEQUENTIAL, CUSTOM
}
```

### Enhanced DTOs with Validation

```kotlin
data class CreateStationDto(
    @field:NotBlank(message = "Station name is required")
    @field:Size(min = 1, max = 100, message = "Station name must be between 1 and 100 characters")
    val name: String,

    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null,

    @field:NotNull(message = "Latitude is required")
    @field:DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @field:DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    val latitude: Double,

    @field:NotNull(message = "Longitude is required")
    @field:DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @field:DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    val longitude: Double,

    @field:Size(max = 200, message = "Address cannot exceed 200 characters")
    val address: String? = null,

    @field:Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    val phone: String? = null
)
```

### ID Generation Service

```kotlin
interface IdGenerationService {
    fun generateStationId(): String
}

@Service
class StationIdGenerationService(
    private val stationProperties: StationProperties,
    private val stationRepository: StationRepository
) : IdGenerationService {

    override fun generateStationId(): String {
        return when (stationProperties.idGenerationStrategy) {
            IdGenerationStrategy.UUID -> "${stationProperties.idPrefix}${UUID.randomUUID()}"
            IdGenerationStrategy.SEQUENTIAL -> generateSequentialId()
            IdGenerationStrategy.CUSTOM -> generateCustomId()
        }
    }
}
```

### Enhanced StationService

```kotlin
@Service
@Transactional
class StationService(
    private val stationRepository: StationRepository,
    private val idGenerationService: IdGenerationService,
    private val stationProperties: StationProperties,
    private val stationValidator: StationValidator
) {

    fun createStation(createDto: CreateStationDto): StationDto {
        // Validation is handled by @Valid in controller, but additional business validation here
        stationValidator.validateForCreation(createDto)

        val stationId = idGenerationService.generateStationId()

        // Check for duplicates based on business rules
        if (stationRepository.existsByNameAndLatitudeAndLongitude(
            createDto.name, createDto.latitude, createDto.longitude)) {
            throw StationAlreadyExistsException("${createDto.name} at coordinates (${createDto.latitude}, ${createDto.longitude})")
        }

        val station = Station(
            id = stationId,
            name = createDto.name,
            description = createDto.description,
            latitude = createDto.latitude,
            longitude = createDto.longitude,
            address = createDto.address,
            phone = createDto.phone,
            status = stationProperties.defaultStatus.displayName,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedStation = stationRepository.save(station)
        logger.info("Created station with ID: {} and name: {}", savedStation.id, savedStation.name)

        return savedStation.toDto()
    }

    fun deleteById(id: String) {
        try {
            stationRepository.deleteById(id)
            logger.info("Deleted station with ID: {}", id)
        } catch (e: EmptyResultDataAccessException) {
            throw StationNotFoundException(id)
        }
    }
}
```

### Custom Validator

```kotlin
@Component
class StationValidator(
    private val stationRepository: StationRepository,
    private val stationProperties: StationProperties
) {

    fun validateForCreation(createDto: CreateStationDto) {
        validateCoordinates(createDto.latitude, createDto.longitude)
        validateBusinessRules(createDto)
    }

    fun validateForUpdate(updateDto: UpdateStationDto, existingStation: Station) {
        updateDto.latitude?.let { lat ->
            updateDto.longitude?.let { lng ->
                validateCoordinates(lat, lng)
            }
        }
        validateBusinessRules(updateDto, existingStation)
    }

    private fun validateCoordinates(latitude: Double, longitude: Double) {
        if (!isValidCoordinates(latitude, longitude)) {
            throw StationValidationException("coordinates", "Invalid coordinates: ($latitude, $longitude)")
        }
    }

    private fun isValidCoordinates(latitude: Double, longitude: Double): Boolean {
        return latitude >= stationProperties.validation.latitudeMin &&
               latitude <= stationProperties.validation.latitudeMax &&
               longitude >= stationProperties.validation.longitudeMin &&
               longitude <= stationProperties.validation.longitudeMax
    }
}
```

## Error Handling Strategy

### Exception Mapping

- `StationNotFoundException` → HTTP 404 Not Found
- `StationAlreadyExistsException` → HTTP 409 Conflict
- `StationValidationException` → HTTP 400 Bad Request
- `DataIntegrityViolationException` → HTTP 409 Conflict
- Generic exceptions → HTTP 500 Internal Server Error

### Global Exception Handler

```kotlin
@RestControllerAdvice
class StationExceptionHandler {

    @ExceptionHandler(StationNotFoundException::class)
    fun handleStationNotFound(ex: StationNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("STATION_NOT_FOUND", ex.message))
    }

    @ExceptionHandler(StationAlreadyExistsException::class)
    fun handleStationAlreadyExists(ex: StationAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse("STATION_ALREADY_EXISTS", ex.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map {
            FieldError(it.field, it.defaultMessage ?: "Invalid value")
        }
        return ResponseEntity.badRequest()
            .body(ValidationErrorResponse("VALIDATION_FAILED", "Input validation failed", errors))
    }
}
```

## Data Models

### Enhanced Station Entity

```kotlin
@Entity
@Table(name = "stations")
data class Station(
    @Id
    val id: String,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(length = 500)
    val description: String? = null,

    @Column(nullable = false, precision = 10, scale = 8)
    val latitude: Double,

    @Column(nullable = false, precision = 11, scale = 8)
    val longitude: Double,

    @Column(length = 200)
    val address: String? = null,

    @Column(length = 20)
    val phone: String? = null,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val status: StationStatus,

    @Column(nullable = false)
    val createdAt: LocalDateTime,

    @Column(nullable = false)
    val updatedAt: LocalDateTime
) {
    fun toDto(): StationDto = StationDto(
        id = id,
        name = name,
        description = description,
        latitude = latitude,
        longitude = longitude,
        address = address,
        phone = phone,
        status = status.displayName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
```

### Repository Enhancements

```kotlin
interface StationRepository : JpaRepository<Station, String> {

    fun existsByNameAndLatitudeAndLongitude(name: String, latitude: Double, longitude: Double): Boolean

    fun findByStatus(status: StationStatus): List<Station>

    @Query("SELECT s FROM Station s WHERE " +
           "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:status IS NULL OR s.status = :status)")
    fun findByFilters(
        @Param("name") name: String?,
        @Param("status") status: StationStatus?
    ): List<Station>

    @Modifying
    @Query("UPDATE Station s SET s.status = :status, s.updatedAt = :updatedAt WHERE s.id = :id")
    fun updateStatus(@Param("id") id: String, @Param("status") status: StationStatus, @Param("updatedAt") updatedAt: LocalDateTime): Int
}
```

## Configuration Management

### Application Properties Structure

```yaml
app:
  station:
    id-prefix: ${STATION_ID_PREFIX:stn_}
    id-generation-strategy: ${STATION_ID_STRATEGY:UUID}
    default-status: ${STATION_DEFAULT_STATUS:ACTIVE}
    validation:
      name-min-length: ${STATION_NAME_MIN:1}
      name-max-length: ${STATION_NAME_MAX:100}
      description-max-length: ${STATION_DESC_MAX:500}
      latitude-min: ${STATION_LAT_MIN:-90.0}
      latitude-max: ${STATION_LAT_MAX:90.0}
      longitude-min: ${STATION_LNG_MIN:-180.0}
      longitude-max: ${STATION_LNG_MAX:180.0}
```

## Testing Strategy

### Unit Tests

1. **Service Layer Tests**: Mock dependencies, test business logic
2. **Validation Tests**: Test all validation scenarios
3. **Exception Handling Tests**: Verify proper exception throwing
4. **ID Generation Tests**: Test different generation strategies

### Integration Tests

1. **Repository Tests**: Test database operations
2. **Controller Tests**: Test HTTP endpoints with validation
3. **End-to-End Tests**: Complete station lifecycle testing

### Performance Tests

1. **Bulk Operations**: Test performance with large datasets
2. **Concurrent Access**: Test thread safety
3. **Database Performance**: Query optimization validation

This design maintains backward compatibility while significantly improving the robustness, maintainability, and configurability of the StationService.
