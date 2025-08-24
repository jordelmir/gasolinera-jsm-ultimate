# Project Structure & Organization

## Repository Layout

This is an Nx monorepo with a hybrid structure combining frontend applications managed by Nx and backend microservices managed by Gradle.

### Root Level

```
├── apps/                   # Frontend applications (Nx managed)
├── services/              # Backend microservices (Gradle managed)
├── packages/              # Shared libraries and SDKs
├── infra/                 # Infrastructure as code
├── ops/                   # Operational scripts and tools
├── integration-tests/     # End-to-end integration tests
├── docs/                  # Documentation
└── build files           # Gradle, Nx, Docker configs
```

## Frontend Applications (`apps/`)

### Web Applications (Next.js + TypeScript)

- `admin/` - Admin dashboard for system operators (port 3000)
- `advertiser/` - Portal for advertisers to manage campaigns (port 3001)
- `owner-dashboard/` - Dashboard for gas station owners (port 3002)

### Mobile Applications (React Native + Expo)

- `mobile/` - Main customer mobile app
- `client-mobile/` - Customer-specific mobile app
- `employee-mobile/` - Employee interface for gas stations

### E2E Testing

- `admin-e2e/` - End-to-end tests for admin dashboard

## Backend Services (`services/`)

Each service follows Spring Boot conventions with Kotlin:

```
services/[service-name]/
├── src/main/kotlin/com/gasolinerajsm/[service]/
│   ├── [Service]Application.kt        # Main application class
│   ├── controller/                    # REST controllers
│   ├── service/                       # Business logic
│   ├── repository/                    # Data access layer
│   ├── model/ or domain/              # Domain entities
│   ├── dto/                          # Data transfer objects
│   ├── config/                       # Configuration classes
│   └── exception/                    # Custom exceptions
├── src/main/resources/
│   ├── application.yml               # Configuration
│   ├── db/migration/                 # Flyway migrations
│   └── logback-spring.xml           # Logging config
├── build.gradle.kts                 # Gradle build script
└── Dockerfile                       # Container definition
```

### Core Services

- `api-gateway/` - Entry point, routing, authentication
- `auth-service/` - User management and JWT tokens
- `station-service/` - Gas station and location management
- `coupon-service/` - QR code generation and validation
- `redemption-service/` - Points and rewards processing
- `ad-engine/` - Advertisement serving and analytics
- `raffle-service/` - Lottery and prize management

## Shared Code (`packages/`)

- `shared/` - Common TypeScript utilities and types
- `internal-sdk/` - Generated Kotlin SDK for inter-service communication
- `temp-sdk/` - Temporary SDK during development
- `proto/` - Protocol buffer definitions

## Infrastructure (`infra/`)

```
infra/
├── helm/                  # Kubernetes Helm charts
├── terraform/             # Infrastructure as code
├── docker/                # Docker configurations
├── nginx/                 # Reverse proxy configs
├── monitoring/            # Prometheus, Grafana configs
└── argocd/               # GitOps deployment configs
```

## Operations (`ops/`)

```
ops/
├── scripts/
│   ├── dev/              # Development utilities
│   └── qr/               # QR code generation tools
├── debezium/             # Change data capture configs
└── key-management/       # Cryptographic key utilities
```

## Naming Conventions

### Backend (Kotlin)

- **Packages**: `com.gasolinerajsm.[service].[layer]`
- **Classes**: PascalCase (`UserController`, `AuthService`)
- **Files**: Match class names (`UserController.kt`)
- **Database**: snake_case tables and columns
- **Environment Variables**: UPPER_SNAKE_CASE

### Frontend (TypeScript)

- **Components**: PascalCase (`UserDashboard.tsx`)
- **Files**: kebab-case for pages (`user-profile.tsx`)
- **Directories**: kebab-case (`user-management/`)
- **CSS Classes**: Tailwind utilities, kebab-case for custom classes

### API Conventions

- **Endpoints**: RESTful with kebab-case (`/api/user-profiles`)
- **JSON Fields**: camelCase in responses
- **HTTP Methods**: Standard REST verbs (GET, POST, PUT, DELETE)

## Configuration Patterns

### Backend Services

- Use `application.yml` for configuration
- Environment-specific overrides with profiles
- Secrets managed through HashiCorp Vault
- Health checks at `/actuator/health`
- Metrics at `/actuator/prometheus`

### Frontend Applications

- Environment variables in `.env` files
- Next.js configuration in `next.config.js`
- Tailwind configuration in `tailwind.config.ts`
- TypeScript configuration extends from `tsconfig.base.json`

## Development Workflow

1. **Backend Changes**: Modify service code, run tests with `./gradlew test`
2. **Frontend Changes**: Use Nx commands like `nx serve [app]`
3. **Full Stack**: Use `make dev` to start entire environment
4. **Database Changes**: Create Flyway migrations in `src/main/resources/db/migration/`
5. **API Changes**: Update OpenAPI specs, regenerate SDKs with Gradle tasks

## Testing Structure

- **Unit Tests**: Co-located with source code in `src/test/`
- **Integration Tests**: In `integration-tests/` directory
- **E2E Tests**: In `apps/[app]-e2e/` directories
- **API Tests**: Use generated SDK clients for consistency
