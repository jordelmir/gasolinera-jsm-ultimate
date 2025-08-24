# Requirements Document - OpenAPI Client Optimization

## Introduction

This feature aims to optimize and professionalize the OpenAPI client generation for all services in the Gasolinera JSM monorepo. The goal is to ensure consistency, reusability, and production readiness across all API clients while establishing a scalable pattern for future services.

## Requirements

### Requirement 1: Centralized Plugin Configuration

**User Story:** As a developer, I want all Kotlin and Spring Boot plugins centralized in the root build.gradle.kts, so that I can maintain consistent versions and avoid duplication across subprojects.

#### Acceptance Criteria

1. WHEN the root build.gradle.kts is configured THEN all Kotlin plugins (`jvm`, `plugin.spring`, `plugin.jpa`) SHALL be defined with `apply false`
2. WHEN the root build.gradle.kts is configured THEN Spring Boot plugin SHALL be defined with `apply false` to prevent conflicts
3. WHEN subprojects need these plugins THEN they SHALL apply them individually without version specification
4. WHEN dependency management is configured THEN Spring Cloud BOM SHALL be applied to all subprojects consistently

### Requirement 2: OpenAPI Specification Generation

**User Story:** As a developer, I want OpenAPI specifications automatically generated from existing controllers, so that I can maintain up-to-date API documentation without manual effort.

#### Acceptance Criteria

1. WHEN a service has REST controllers THEN an openapi.yaml file SHALL be generated automatically
2. WHEN the openapi.yaml is generated THEN it SHALL include all endpoints, request/response models, and validation constraints
3. WHEN the specification is generated THEN it SHALL follow OpenAPI 3.0 standards
4. WHEN multiple services exist THEN each SHALL have its own openapi.yaml file in the service directory

### Requirement 3: Consistent Client Generation Configuration

**User Story:** As a developer, I want consistent configuration options for all API clients, so that generated clients behave uniformly across services.

#### Acceptance Criteria

1. WHEN commonConfigOptions is defined THEN it SHALL include dateLibrary, useCoroutines, useBeanValidation, interfaceOnly, skipFormModel, skipDefaultInterface, useTags, and openApiNullable
2. WHEN client generation tasks are created THEN they SHALL use the common configuration options
3. WHEN new services are added THEN they SHALL automatically inherit the common configuration
4. WHEN configuration needs to be updated THEN it SHALL be changed in one place and apply to all clients

### Requirement 4: Dynamic Service Registration

**User Story:** As a developer, I want to add new API clients by simply updating a service list, so that I don't need to duplicate task configuration code.

#### Acceptance Criteria

1. WHEN the apiClients list is defined THEN it SHALL contain all active services (auth-service, redemption-service, ad-engine, raffle-service, station-service, coupon-service)
2. WHEN a new service is added to the list THEN a generation task SHALL be automatically created
3. WHEN servicePath and sdkPackage are specified THEN they SHALL follow consistent naming conventions
4. WHEN the list is updated THEN no additional task configuration SHALL be required

### Requirement 5: Proper Package Structure

**User Story:** As a developer, I want generated clients to follow consistent package naming, so that imports and dependencies are predictable across the codebase.

#### Acceptance Criteria

1. WHEN clients are generated THEN apiPackage SHALL follow the pattern `com.gasolinerajsm.sdk.{service}.api`
2. WHEN clients are generated THEN modelPackage SHALL follow the pattern `com.gasolinerajsm.sdk.{service}.model`
3. WHEN output directories are created THEN they SHALL be in `${buildDir}/generated/{service}-client`
4. WHEN package names are defined THEN they SHALL be consistent with the existing project structure

### Requirement 6: Build Integration and Verification

**User Story:** As a developer, I want to verify that all client generation tasks work correctly, so that I can ensure the build process is reliable.

#### Acceptance Criteria

1. WHEN generation tasks are executed THEN they SHALL complete without errors
2. WHEN `gradlew generate{Name}Client` is run THEN it SHALL produce valid Kotlin client code
3. WHEN all clients need to be regenerated THEN a single command SHALL regenerate all clients
4. WHEN the build process runs THEN it SHALL validate that all required openapi.yaml files exist

### Requirement 7: Production Readiness

**User Story:** As a developer, I want the client generation process to be production-ready, so that it can be used in CI/CD pipelines and deployment processes.

#### Acceptance Criteria

1. WHEN the generator is configured THEN it SHALL use the most appropriate generatorName for Kotlin Spring projects
2. WHEN clients are generated THEN they SHALL include proper error handling and validation
3. WHEN the build process runs THEN it SHALL be deterministic and reproducible
4. WHEN documentation is generated THEN it SHALL include usage examples and integration guides

### Requirement 8: Missing Service Integration

**User Story:** As a developer, I want all existing services included in the client generation, so that no service is left without a proper SDK.

#### Acceptance Criteria

1. WHEN the service list is reviewed THEN coupon-service SHALL be included in the apiClients list
2. WHEN api-gateway exists THEN it SHALL be evaluated for inclusion or exclusion with proper justification
3. WHEN services are missing openapi.yaml files THEN they SHALL be generated from existing controllers
4. WHEN all services are configured THEN the build SHALL validate completeness

### Requirement 9: Automated Testing Integration

**User Story:** As a developer, I want generated clients to include test capabilities, so that I can verify API integration works correctly.

#### Acceptance Criteria

1. WHEN clients are generated THEN they SHALL include test utilities and mock capabilities
2. WHEN integration tests are needed THEN generated clients SHALL support test configuration
3. WHEN API changes occur THEN tests SHALL validate backward compatibility
4. WHEN clients are used THEN they SHALL provide clear error messages for debugging
