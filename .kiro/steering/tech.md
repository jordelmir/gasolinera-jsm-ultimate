# Technology Stack & Build System

## Build System

- **Primary**: Nx monorepo for frontend applications and shared packages
- **Backend**: Gradle with Kotlin DSL for microservices
- **Containerization**: Docker with multi-stage builds
- **Orchestration**: Docker Compose for local development, Kubernetes + Helm for production

## Backend Stack

- **Language**: Kotlin with Java 17
- **Framework**: Spring Boot 3.3.3 with Spring Cloud
- **Database**: PostgreSQL with Flyway migrations
- **Cache**: Redis for sessions and distributed caching
- **Messaging**: RabbitMQ with Debezium for Change Data Capture
- **Security**: JWT tokens, HashiCorp Vault for secrets management
- **Observability**: OpenTelemetry + Jaeger for distributed tracing, Micrometer + Prometheus for metrics

## Frontend Stack

- **Web Applications**: Next.js 14 with TypeScript
- **Mobile Applications**: React Native with Expo
- **Styling**: Tailwind CSS with shadcn/ui components
- **State Management**: Zustand for client state
- **Forms**: React Hook Form with Zod validation
- **HTTP Client**: Axios

## Key Libraries & Frameworks

- **UI Components**: Radix UI primitives with custom Tailwind styling
- **Animation**: Framer Motion, Lottie React
- **Icons**: Lucide React
- **Testing**: Jest, React Testing Library
- **Code Quality**: ESLint, Prettier, Detekt (Kotlin)

## Common Development Commands

### Full Environment

```bash
make dev                    # Start complete development environment
make stop                   # Stop all services
make clean                  # Clean up containers and volumes
make logs                   # View all service logs
```

### Frontend Development

```bash
npm run dev                 # Start owner dashboard (port 3002)
npm run dev:admin          # Start admin panel (port 3000)
nx serve advertiser        # Start advertiser portal (port 3001)
make dev-frontend          # Start all frontend apps
```

### Backend Development

```bash
./gradlew build            # Build all services
./gradlew bootRun          # Run individual service
make test                  # Run all tests
```

### Mobile Development

```bash
make mobile                # Start main mobile app
make client-mobile         # Start client mobile app
make employee-mobile       # Start employee mobile app
```

### Database & Seeding

```bash
make seed                  # Seed database with test data
make seed-coupon-system    # Seed coupon system specifically
```

### Code Quality

```bash
npm run lint               # Lint all projects
npm run format             # Format code with Prettier
npm run lint:fix           # Auto-fix linting issues
```

## Architecture Patterns

- **Microservices**: Domain-driven service boundaries
- **API Gateway**: Single entry point with routing and auth
- **Outbox Pattern**: Eventual consistency with Debezium
- **Circuit Breaker**: Resilience between services
- **CQRS**: Command Query Responsibility Segregation where appropriate

## Development Ports

- API Gateway: 8080
- Auth Service: 8081
- Redemption Service: 8082
- Station Service: 8083
- Ad Engine: 8084
- Raffle Service: 8085
- Coupon Service: 8086
- Admin Dashboard: 3000
- Advertiser Portal: 3001
- Owner Dashboard: 3002
- PostgreSQL: 5432
- Redis: 6379
- RabbitMQ: 5672 (Management: 15672)
- Jaeger UI: 16686
- Vault: 8200
