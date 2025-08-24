# Client Configuration Package

This package provides configuration utilities for the generated OpenAPI clients in the Gasolinera JSM monorepo.

## Features

- ✅ **Environment-specific Configuration**: Development, staging, and production configurations
- ✅ **Authentication Support**: JWT, API Key, and Basic authentication filters
- ✅ **Client Factory**: Centralized client creation with proper configuration
- ✅ **Validation**: Configuration validation utilities
- ✅ **Spring Integration**: Full Spring Boot integration with configuration properties

## Usage

### 1. Add Dependency

```kotlin
// In your service's build.gradle.kts
dependencies {
    implementation(project(":packages:client-config"))
}
```

### 2. Configure Properties

```yaml
# application.yml
gasolinera:
  api:
    baseUrl: 'http://localhost'
    timeout: PT30S
    retries: 3
    auth:
      enabled: true
      clientId: 'your-client-id'
      clientSecret: 'your-client-secret'
    services:
      auth:
        port: 8081
        timeout: PT30S
        retries: 3
        enabled: true
      station:
        port: 8083
        timeout: PT30S
        retries: 3
        enabled: true
```

### 3. Use in Your Service

```kotlin
@Service
class YourService(
    private val authApi: AuthApi,
    private val stationApi: StationApi
) {
    suspend fun doSomething() {
        // Clients are automatically configured with:
        // - Correct base URLs
        // - Authentication
        // - Timeouts and retries
        // - Error handling

        val stations = stationApi.getAllStations()
        // ...
    }
}
```

## Configuration Options

### Service Configuration

Each service can be configured with:

- `baseUrl`: Full URL (overrides global baseUrl + port)
- `port`: Port number (used with global baseUrl if baseUrl not specified)
- `timeout`: Request timeout duration
- `retries`: Number of retry attempts
- `enabled`: Whether the service is enabled

### Authentication Configuration

- `enabled`: Enable/disable authentication
- `tokenUrl`: URL for token requests
- `clientId`: OAuth client ID
- `clientSecret`: OAuth client secret

## Environment Profiles

### Development (`application-development.yml`)

- Longer timeouts for debugging
- Fewer retries for faster feedback
- Debug logging enabled
- Local service URLs

### Staging (`application-staging.yml`)

- Moderate timeouts
- Moderate retry counts
- Info-level logging
- Staging service URLs

### Production (`application-production.yml`)

- Optimized timeouts
- Full retry counts
- Minimal logging
- Production service URLs
- Environment variable support

## Client Factory

The `ClientFactory` creates properly configured clients:

```kotlin
@Autowired
private lateinit var authApi: AuthApi

@Autowired
private lateinit var stationApi: StationApi

// All clients are pre-configured with:
// - Base URLs
// - Authentication filters
// - Error handling
// - Logging
```

## Authentication

### JWT Authentication

Automatically adds `Authorization: Bearer <token>` header:

```kotlin
// Token is managed automatically
val response = authApi.verifyOtp(request)
```

### API Key Authentication

Adds `X-API-Key` header from environment variable:

```bash
export GASOLINERA_API_KEY=your-api-key
```

### Basic Authentication

Uses configured client credentials:

```yaml
gasolinera:
  api:
    auth:
      clientId: 'your-client-id'
      clientSecret: 'your-client-secret'
```

## Validation

Configuration is validated at startup:

```kotlin
val errors = ClientConfigurationUtils.validateConfig(config)
if (errors.isNotEmpty()) {
    throw IllegalArgumentException("Invalid configuration: ${errors.joinToString()}")
}
```

Common validation errors:

- Blank base URL
- Invalid URL protocol (must be http:// or https://)
- Negative timeout
- Negative retry count

## Testing

Test configuration is provided in `application-test.yml`:

```kotlin
@SpringBootTest
@ActiveProfiles("test")
class YourServiceTest {
    // Test configuration automatically loaded
}
```

## Utilities

### Creating Custom Configurations

```kotlin
// Development
val devConfig = ClientConfigurationUtils.createDevelopmentConfig(8081)

// Production
val prodConfig = ClientConfigurationUtils.createProductionConfig("auth")

// Staging
val stagingConfig = ClientConfigurationUtils.createStagingConfig("station")

// Custom
val customConfig = ClientConfigurationUtils.createClientConfig(
    baseUrl = "http://custom:8080",
    timeout = Duration.ofSeconds(60),
    retries = 5,
    enabled = true
)
```

### Configuration Validation

```kotlin
val config = ServiceConfig(...)
val errors = ClientConfigurationUtils.validateConfig(config)

if (errors.isNotEmpty()) {
    println("Configuration errors:")
    errors.forEach { println("- $it") }
}
```

## Integration with Generated Clients

This package is designed to work seamlessly with the generated OpenAPI clients:

1. **Automatic Configuration**: Clients are automatically configured based on environment
2. **Authentication**: JWT tokens are automatically added to requests
3. **Error Handling**: Common errors are handled consistently
4. **Logging**: Request/response logging for debugging

## Best Practices

1. **Environment Variables**: Use environment variables for sensitive configuration in production
2. **Profile-specific Configuration**: Use Spring profiles for environment-specific settings
3. **Validation**: Always validate configuration at startup
4. **Monitoring**: Monitor client performance and errors
5. **Security**: Never hardcode credentials in configuration files

## Troubleshooting

### Common Issues

1. **Client Not Found**: Ensure the client package is in your dependencies
2. **Authentication Failures**: Check client credentials and token validity
3. **Connection Timeouts**: Adjust timeout settings for your environment
4. **Service Unavailable**: Check service URLs and network connectivity

### Debug Logging

Enable debug logging to troubleshoot issues:

```yaml
logging:
  level:
    com.gasolinerajsm.sdk: DEBUG
    org.springframework.web.reactive.function.client: DEBUG
```

This will log:

- Request URLs and headers
- Response status codes
- Authentication token usage
- Configuration values

---

_This package is part of the Gasolinera JSM OpenAPI client generation system._
