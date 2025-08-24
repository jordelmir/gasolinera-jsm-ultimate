# OpenAPI Client Generation - Maintenance Procedures

This document provides comprehensive maintenance procedures for the OpenAPI client generation system in the Gasolinera JSM monorepo.

## Table of Contents

- [Adding New Services](#adding-new-services)
- [Updating Existing Services](#updating-existing-services)
- [Configuration Drift Detection](#configuration-drift-detection)
- [Performance Monitoring](#performance-monitoring)
- [Troubleshooting](#troubleshooting)
- [Regular Maintenance Tasks](#regular-maintenance-tasks)
- [Emergency Procedures](#emergency-procedures)

## Adding New Services

### 1. Service Registry Configuration

When adding a new service to the client generation system:

```kotlin
// In build.gradle.kts, add to serviceRegistry:
val serviceRegistry = listOf(
    // ... existing services
    ServiceConfig(
        name = "NewService",              // PascalCase display name
        servicePath = "new-service",      // kebab-case directory name
        sdkPackage = "com.gasolinerajsm.sdk.newservice", // Package name
        port = 8087,                      // Unique port number
        apiVersion = "v1",                // API version
        hasOpenApi = true,                // Enable OpenAPI generation
        generatorName = "kotlin",         // Generator type
        library = "jvm-spring-webclient"  // Client library
    )
)
```

### 2. Service Build Configuration

Update the service's `build.gradle.kts`:

```kotlin
// services/new-service/build.gradle.kts
plugins {
    id("org.springdoc.openapi-gradle-plugin")
    // ... other plugins
}

dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    // ... other dependencies
}

openApi {
    apiDocsUrl.set("http://localhost:8087/v3/api-docs")
    outputDir.set(file("$projectDir"))
    outputFileName.set("openapi.yaml")
}
```

### 3. Controller Annotations

Ensure proper OpenAPI annotations in controllers:

```kotlin
@RestController
@RequestMapping("/api/v1/newservice")
@Tag(name = "NewService", description = "New service operations")
class NewServiceController {

    @GetMapping
    @Operation(summary = "Get all items", description = "Retrieves all items from the service")
    @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    fun getAllItems(): List<ItemDto> {
        // Implementation
    }

    @PostMapping
    @Operation(summary = "Create item", description = "Creates a new item")
    @ApiResponse(responseCode = "201", description = "Item created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    fun createItem(@Valid @RequestBody item: CreateItemDto): ItemDto {
        // Implementation
    }
}
```

### 4. Validation and Testing

After adding the new service:

```bash
# 1. Verify service is listed
./gradlew listServices

# 2. Generate OpenAPI spec
./gradlew generateNewServiceOpenApi

# 3. Generate client
./gradlew generateNewServiceClient

# 4. Validate client
./gradlew validateNewServiceClient

# 5. Run full verification
./scripts/verify-client-generation.sh
```

### 5. Update Documentation

- Add service to README_OPENAPI_CLIENTS.md
- Update service count in documentation
- Add usage examples if needed

## Updating Existing Services

### 1. API Changes

When updating an existing service's API:

```bash
# 1. Update controller annotations
# 2. Regenerate OpenAPI spec
./gradlew generateAuthOpenApi

# 3. Regenerate client
./gradlew generateAuthClient

# 4. Test changes
./gradlew validateAuthClient
```

### 2. Configuration Changes

When updating service configuration:

```kotlin
// Update in build.gradle.kts serviceRegistry
ServiceConfig(
    name = "Auth",
    servicePath = "auth-service",
    sdkPackage = "com.gasolinerajsm.sdk.auth",
    port = 8081,
    // Updated configuration
    apiVersion = "v2",  // Version change
    library = "jvm-spring-webclient-reactive"  // Library change
)
```

### 3. Breaking Changes

For breaking API changes:

1. **Version the API**: Use different endpoints (`/api/v1/`, `/api/v2/`)
2. **Maintain Backward Compatibility**: Keep old endpoints during transition
3. **Update Clients Gradually**: Generate separate clients for different versions
4. **Document Migration Path**: Provide clear migration instructions

## Configuration Drift Detection

### 1. Automated Checks

Create a validation script to detect configuration drift:

```bash
#!/bin/bash
# scripts/check-configuration-drift.sh

# Check service registry consistency
echo "Checking service registry consistency..."

# Verify all services in registry have corresponding directories
for service in auth-service station-service coupon-service redemption-service ad-engine raffle-service; do
    if [ ! -d "services/$service" ]; then
        echo "❌ Service directory missing: services/$service"
        exit 1
    fi
done

# Check for services not in registry
for service_dir in services/*/; do
    service_name=$(basename "$service_dir")
    if ! grep -q "$service_name" build.gradle.kts; then
        echo "⚠️  Service not in registry: $service_name"
    fi
done

# Verify OpenAPI specs exist
for service in auth-service station-service coupon-service redemption-service ad-engine raffle-service; do
    if [ ! -f "services/$service/openapi.yaml" ]; then
        echo "⚠️  OpenAPI spec missing: services/$service/openapi.yaml"
    fi
done

echo "✅ Configuration drift check completed"
```

### 2. CI/CD Integration

Add to GitHub Actions workflow:

```yaml
- name: Check Configuration Drift
  run: |
    chmod +x scripts/check-configuration-drift.sh
    ./scripts/check-configuration-drift.sh
```

### 3. Regular Audits

Schedule monthly audits to check:

- Service registry vs actual services
- OpenAPI spec freshness
- Generated client consistency
- Documentation accuracy

## Performance Monitoring

### 1. Build Performance Metrics

Monitor key metrics:

```bash
# Check generation times
./gradlew generateAllClients --profile

# Check up-to-date status
./gradlew checkGeneratedClientsUpToDate

# Run performance optimization
./scripts/optimize-build-performance.sh
```

### 2. Performance Thresholds

Set performance thresholds:

- **Individual Client Generation**: < 5 seconds
- **All Clients Generation**: < 30 seconds
- **Build Cache Hit Rate**: > 80%
- **Parallel Efficiency**: > 70%

### 3. Performance Alerts

Create alerts for performance degradation:

```bash
# Example monitoring script
GENERATION_TIME=$(./gradlew generateAllClients --quiet 2>&1 | grep "BUILD SUCCESSFUL" | awk '{print $4}')
if [ "$GENERATION_TIME" -gt 30 ]; then
    echo "⚠️  Performance degradation detected: ${GENERATION_TIME}s"
    # Send alert
fi
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Client Generation Fails

**Symptoms**: `GenerateTask` fails with errors

**Diagnosis**:

```bash
./gradlew generateAuthClient --debug --stacktrace
```

**Solutions**:

- Check OpenAPI spec validity
- Verify generator configuration
- Update OpenAPI Generator version
- Check available disk space and memory

#### 2. OpenAPI Spec Not Found

**Symptoms**: "OpenAPI specification not found" error

**Solutions**:

```bash
# Generate spec from running service
./gradlew generateAuthOpenApi

# Or create placeholder
./gradlew generateAllOpenApiSpecs
```

#### 3. Build Performance Issues

**Symptoms**: Slow build times, high memory usage

**Solutions**:

```bash
# Optimize build
./scripts/optimize-build-performance.sh

# Clean and rebuild
./gradlew cleanGeneratedClients generateAllClients --parallel

# Check system resources
free -h  # Linux
vm_stat  # macOS
```

#### 4. Configuration Cache Issues

**Symptoms**: Configuration cache errors

**Solutions**:

```bash
# Disable configuration cache temporarily
echo "org.gradle.unsafe.configuration-cache=false" >> gradle.properties

# Or clear cache
rm -rf .gradle/configuration-cache
```

### Diagnostic Commands

```bash
# System diagnostics
./gradlew --version
./gradlew properties | grep -E "(parallel|cache|workers)"
java -version

# Project diagnostics
./gradlew listServices
./gradlew checkGeneratedClientsUpToDate
./scripts/verify-client-generation.sh

# Performance diagnostics
./gradlew generateAllClients --profile --scan
./scripts/optimize-build-performance.sh
```

## Regular Maintenance Tasks

### Daily Tasks

- [ ] Monitor CI/CD pipeline for failures
- [ ] Check for new service additions
- [ ] Review performance metrics

### Weekly Tasks

- [ ] Run full client generation verification
- [ ] Check for OpenAPI spec updates
- [ ] Review build performance trends
- [ ] Update documentation if needed

### Monthly Tasks

- [ ] Audit service registry consistency
- [ ] Review and update dependencies
- [ ] Performance optimization review
- [ ] Clean up old build artifacts

### Quarterly Tasks

- [ ] Update OpenAPI Generator version
- [ ] Review and update configuration
- [ ] Comprehensive performance analysis
- [ ] Documentation review and updates

### Maintenance Checklist

```bash
#!/bin/bash
# scripts/maintenance-checklist.sh

echo "🔧 OpenAPI Client Generation Maintenance Checklist"
echo "=================================================="

# 1. Verify all services
echo "1. Verifying service configuration..."
./gradlew listServices

# 2. Check client generation
echo "2. Checking client generation..."
./scripts/verify-client-generation.sh

# 3. Performance check
echo "3. Checking performance..."
./gradlew checkGeneratedClientsUpToDate

# 4. Configuration drift
echo "4. Checking configuration drift..."
./scripts/check-configuration-drift.sh

# 5. Clean up old artifacts
echo "5. Cleaning up old artifacts..."
find build/generated -name "*.tmp" -delete
find build/generated -name "*.log" -delete

echo "✅ Maintenance checklist completed"
```

## Emergency Procedures

### 1. Complete System Failure

If the entire client generation system fails:

```bash
# 1. Stop all processes
./gradlew --stop

# 2. Clean everything
./gradlew clean cleanGeneratedClients
rm -rf .gradle/caches
rm -rf build-cache

# 3. Regenerate from scratch
./gradlew generateAllOpenApiSpecs
./gradlew generateAllClients

# 4. Verify system
./scripts/verify-client-generation.sh
```

### 2. Individual Service Failure

If a specific service client fails:

```bash
# 1. Clean service-specific artifacts
rm -rf build/generated/auth-service-client
rm -f services/auth-service/openapi.yaml

# 2. Regenerate service
./gradlew generateAuthOpenApi
./gradlew generateAuthClient

# 3. Validate
./gradlew validateAuthClient
```

### 3. Performance Emergency

If build times become unacceptable:

```bash
# 1. Immediate relief
./gradlew generateAllClients --parallel --build-cache

# 2. System optimization
./scripts/optimize-build-performance.sh

# 3. Resource check
# Ensure adequate memory and CPU
# Consider scaling build infrastructure
```

### 4. Rollback Procedures

If a change breaks the system:

```bash
# 1. Revert configuration changes
git checkout HEAD~1 -- build.gradle.kts

# 2. Regenerate with previous config
./gradlew generateAllClients

# 3. Verify rollback
./scripts/verify-client-generation.sh
```

## Monitoring and Alerting

### 1. Key Metrics to Monitor

- **Build Success Rate**: Should be > 95%
- **Generation Time**: Should be < 30 seconds total
- **Client File Count**: Should be consistent per service
- **Error Rate**: Should be < 5%

### 2. Alert Conditions

Set up alerts for:

- Build failures
- Performance degradation (> 50% increase in build time)
- Missing OpenAPI specifications
- Configuration drift detection

### 3. Monitoring Dashboard

Create a dashboard showing:

- Build success/failure trends
- Performance metrics over time
- Service health status
- Configuration consistency status

## Documentation Maintenance

### 1. Keep Documentation Current

- Update README files when adding services
- Maintain accurate command examples
- Update troubleshooting guides with new issues
- Keep performance benchmarks current

### 2. Version Documentation

- Tag documentation versions with releases
- Maintain migration guides for major changes
- Archive old documentation appropriately

### 3. User Feedback

- Collect feedback from developers using the system
- Update procedures based on real-world usage
- Maintain FAQ based on common questions

---

## Quick Reference

### Essential Commands

```bash
# List all services
./gradlew listServices

# Generate all clients
./gradlew generateAllClients --parallel

# Verify system
./scripts/verify-client-generation.sh

# Check performance
./gradlew checkGeneratedClientsUpToDate

# Optimize system
./scripts/optimize-build-performance.sh

# Emergency reset
./gradlew clean cleanGeneratedClients && ./gradlew generateAllClients
```

### Support Contacts

- **System Owner**: Development Team
- **CI/CD Issues**: DevOps Team
- **Performance Issues**: Infrastructure Team
- **Documentation**: Technical Writing Team

---

_This maintenance guide is part of the Gasolinera JSM OpenAPI client generation system. Keep it updated as the system evolves._
