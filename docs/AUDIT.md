# Project Audit Report

This document summarizes the initial audit of the `gasolinera-jsm-ultimate` repository, covering its technology stack, service architecture, dependencies, environment configurations, and existing operational scripts.

## 1. Technology Stack

*   **Monorepo Tool:** Nx
*   **Backend:** Kotlin, Spring Boot 3, Java 17
*   **Frontend (Web):** Next.js, TypeScript, Tailwind CSS (Admin, Advertiser, Owner Dashboard)
*   **Frontend (Mobile):** Expo, React Native (Client Mobile, Employee Mobile, Mobile)
*   **Infrastructure:** Docker, Docker Compose, `render.yaml`, `vercel.json`
*   **Database:** PostgreSQL
*   **Caching/Messaging:** Redis, RabbitMQ
*   **Observability:** Jaeger, Micrometer, OpenTelemetry, Prometheus, Logstash Logback Encoder
*   **Secrets Management:** HashiCorp Vault (in development setup)

## 2. Service Map and Ports

The application is composed of several microservices and core infrastructure components, primarily managed via Docker Compose.

### Core Infrastructure Services

| Service           | Description                               | Exposed Ports (Host:Container) |
| :---------------- | :---------------------------------------- | :----------------------------- |
| `postgres`        | PostgreSQL Database                       | `5432:5432`                    |
| `redis`           | Redis Cache/Message Broker                | `6379:6379`                    |
| `rabbitmq`        | RabbitMQ Message Broker                   | `5672:5672` (AMQP), `15672:15672` (Management UI) |
| `jaeger`          | Jaeger Tracing UI and API                 | `16686:16686` (UI), `14268:14268` (API) |
| `vault`           | HashiCorp Vault (Secrets Management)      | `8200:8200`, `8201:8201`       |
| `debezium-connect`| Debezium Kafka Connect (CDC)              | `8083:8083`                    |

### Backend Services

| Service           | Description                               | Exposed Ports (Host:Container) |
| :---------------- | :---------------------------------------- | :----------------------------- |
| `api-gateway`     | Central API Gateway                       | `8080:8080`                    |
| `auth-service`    | User Authentication and Authorization     | `8081:8080`                    |
| `redemption-service`| Coupon Redemption Logic                   | `8082:8080`                    |
| `station-service` | Gas Station Management                    | `8083:8080`                    |
| `ad-engine`       | Advertisement Delivery Engine             | `8084:8080`                    |
| `raffle-service`  | Raffle and Prize Management               | `8085:8080`                    |
| `coupon-service`  | Coupon Generation and Management          | `8086:8080`                    |

## 3. Critical Dependencies, Vulnerabilities, and Secrets

### Environment Variables (`.env.example`)

The `.env.example` file outlines the necessary environment variables:

*   **Common:** `SPRING_PROFILES_ACTIVE`, `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `REDIS_URL`, `KAFKA_BOOTSTRAP_SERVERS`
*   **Secrets (to be replaced):** `JWT_SECRET`, `QR_PUBLIC_KEY`
*   **Service-Specific:** `GEOfence_RADIUS_METERS` (Redemption), `AD_FALLBACK_URL` (Ad Engine)

### Secrets Management Observations

*   `JWT_SECRET` and `QR_PUBLIC_KEY` are explicitly marked for replacement in production, indicating they are sensitive.
*   HashiCorp Vault is integrated into the `docker-compose.yml` for backend services, suggesting an intent for centralized secret management. However, the `VAULT_TOKEN` (`myroottoken`) is hardcoded in `docker-compose.yml`, which is a security risk for production environments. This needs to be externalized and secured.

### Frontend Dependencies (from `package.json` files)

*   **Frameworks/Libraries:** Next.js, React, React-DOM, Expo, React Native, Zustand, React Hook Form, Zod, Radix UI, Lucide React, Tailwind CSS.
*   **Utilities:** `clsx`, `tailwind-merge`, `tailwindcss-animate`, `react-toastify`, `axios`.
*   **Mobile Specific:** `@expo/vector-icons`, `@react-navigation/native`, `expo-av`, `expo-barcode-scanner`, `expo-camera`, `expo-constants`, `expo-font`, `expo-linking`, `expo-notifications`, `expo-splash-screen`, `expo-status-bar`, `expo-system-ui`, `expo-web-browser`, `react-native-gesture-handler`, `react-native-reanimated`, `react-native-safe-area-context`, `react-native-screens`, `react-native-web`, `react-native-qrcode-svg`, `react-native-svg`, `react-native-toast-message`, `react-native-webview`.

### Backend Dependencies (from `build.gradle.kts` files)

*   **Spring Boot Ecosystem:** `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-actuator`, `spring-boot-starter-data-redis`, `spring-boot-starter-amqp`, `spring-boot-starter-webflux`.
*   **Kotlin:** `kotlin-reflect`, `jackson-module-kotlin`, `kotlinx-coroutines-core`, `kotlinx-coroutines-reactor`.
*   **Spring Cloud:** `spring-cloud-starter-gateway`, `spring-cloud-starter-loadbalancer`, `spring-cloud-starter-vault-config`, `spring-cloud-starter-circuitbreaker-reactor-resilience4j`.
*   **Database:** `postgresql`, `flyway-core`, `flyway-database-postgresql`.
*   **Security:** `spring-security-oauth2-resource-server`, `spring-security-oauth2-jose`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`.
*   **Observability:** `micrometer-registry-prometheus`, `micrometer-tracing-bridge-brave`, `opentelemetry-exporter-otlp`, `logstash-logback-encoder`.
*   **API Documentation:** `springdoc-openapi-starter-webmvc-ui`, `springdoc-openapi-starter-common`.
*   **Testing:** `spring-boot-starter-test`, `junit-jupiter`, `mockito-kotlin`, `testcontainers` (for PostgreSQL, Redis), `spring-security-test`, `spring-rabbit-test`, `mockk`, `springmockk`.
*   **Other:** `com.google.zxing` (QR Code), `internal-sdk` (Redemption service dependency).

### Potential Vulnerabilities / SCA

*   No explicit SCA tools are currently integrated into the build process (e.g., OWASP Dependency-Check, Snyk). This is a critical gap for production readiness.
*   `detekt` (Kotlin linter/static analysis) is commented out in some `build.gradle.kts` files due to version conflicts, indicating a potential lack of consistent code quality enforcement.

## 4. Status of Scripts and Gaps

The `Makefile` provides a centralized entry point for various development and operational tasks:

*   **Development:** `dev`, `dev-frontend`, `stop`, `clean`, `logs`, `mobile`, `client-mobile`, `employee-mobile`, `owner-dashboard`, `dev-mobile`, `dev-web`.
*   **Build & Test:** `build-all`, `test`, `lint`, `format`, `check-deps`.
*   **Data:** `seed`, `seed-coupon-system`, `db-migrate`, `db-backup`, `db-restore`.
*   **Kubernetes (Local):** `k8s-up`, `k8s-down` (uses Helm).
*   **Deployment (Docker Compose):** `deploy-staging`, `deploy-production`.

### Gaps and Observations

*   **CI/CD Integration:** The `deploy-staging` and `deploy-production` targets use `docker-compose`, which is suitable for local testing but not for automated, robust deployments to cloud platforms like Render.com or Vercel. A proper CI/CD pipeline (e.g., GitHub Actions) is needed for this.
*   **Kafka Setup:** While `KAFKA_BOOTSTRAP_SERVERS` is in `.env.example` and Debezium is present, Kafka itself is not defined or started in the provided `docker-compose` files. This needs to be addressed if Debezium is to be fully functional.
*   **Code Quality:** The disabled `detekt` configuration suggests a need to re-evaluate and integrate static analysis tools for Kotlin code.
*   **Dependency Management:** `check-deps` is available, but automated dependency updates and vulnerability scanning are missing.

## 5. Health Endpoints, Metrics, and Tracing

### Health Endpoints

*   Most Spring Boot services expose `/actuator/health` endpoints.
*   Health checks are defined in `docker-compose.yml` and `docker-compose.dev.yml` for `api-gateway`, `auth-service`, `coupon-service`, `station-service`, `redemption-service`, `ad-engine`, and `raffle-service`.

### Metrics

*   `io.micrometer:micrometer-registry-prometheus` is included in most backend services, indicating readiness for Prometheus integration.

### Tracing

*   `io.micrometer:micrometer-tracing-bridge-brave` and `io.opentelemetry:opentelemetry-exporter-otlp` are present in backend services.
*   Jaeger is configured in `docker-compose.yml` (`jaeger` service), suggesting OpenTelemetry traces are collected and viewable.

### Logging

*   `net.logstash.logback:logstash-logback-encoder` is used for structured JSON logging, which is beneficial for centralized log management.

---
**Next Steps:**
1.  Execute `make build-all` and `make dev` to identify immediate compilation and runtime issues.
2.  Address identified issues by creating `fix/*` PRs.
3.  Update `STATUS_REPORT.md` with progress.