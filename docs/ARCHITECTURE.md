# Arquitectura del Sistema - Gasolinera JSM

## Visión General

Gasolinera JSM es una plataforma de microservicios diseñada para digitalizar y monetizar la experiencia de recarga de combustible. El sistema utiliza una arquitectura distribuida con patrones modernos de desarrollo.

## Componentes Principales

### Backend Services (Kotlin + Spring Boot)

1. **API Gateway** (Puerto 8080)

   - Punto de entrada único para todas las requests
   - Enrutamiento y balanceo de carga
   - Autenticación y autorización
   - Rate limiting y circuit breaker

2. **Auth Service** (Puerto 8081)

   - Gestión de usuarios y autenticación
   - JWT token management
   - Roles y permisos

3. **Station Service**

   - Gestión de estaciones de servicio
   - Información de ubicaciones y servicios
   - Integración con sistemas de punto de venta

4. **Redemption Service** (Puerto 8082)

   - Procesamiento de canjes de puntos
   - Gestión de recompensas
   - Historial de transacciones

5. **Ad Engine** (Puerto 8084)

   - Motor de publicidad personalizada
   - Segmentación de audiencias
   - Métricas de engagement

6. **Raffle Service** (Puerto 8085)
   - Sistema de sorteos y promociones
   - Gestión de participaciones
   - Algoritmos de selección aleatoria

### Frontend Applications

1. **Admin Dashboard** (Puerto 3000)

   - Panel de administración para operadores
   - Gestión de estaciones y usuarios
   - Analytics y reportes

2. **Advertiser Portal** (Puerto 3001)

   - Portal para anunciantes
   - Creación y gestión de campañas
   - Métricas de rendimiento

3. **Mobile App** (React Native + Expo)
   - Aplicación móvil para usuarios finales
   - Escaneo de códigos QR
   - Programa de fidelización

### Infraestructura

- **PostgreSQL**: Base de datos principal
- **Redis**: Cache y gestión de sesiones
- **RabbitMQ**: Mensajería asíncrona
- **Vault**: Gestión de secretos
- **Jaeger**: Tracing distribuido
- **Debezium**: Change Data Capture

## Patrones de Diseño

### Outbox Pattern

Garantiza la consistencia eventual entre servicios mediante:

- Transacciones locales con eventos en tabla outbox
- Debezium para captura de cambios
- Publicación automática a RabbitMQ

### Circuit Breaker

Implementado en el API Gateway para:

- Prevenir cascadas de fallos
- Fallback automático
- Recuperación gradual

### CQRS (Command Query Responsibility Segregation)

Separación de operaciones de lectura y escritura para:

- Optimización de rendimiento
- Escalabilidad independiente
- Modelos de datos especializados

## Observabilidad

### Tracing Distribuido

- OpenTelemetry para instrumentación
- Jaeger para visualización de traces
- Correlación de requests entre servicios

### Métricas

- Métricas de aplicación con Micrometer
- Métricas de infraestructura con Prometheus
- Dashboards en Grafana

### Logging

- Structured logging con Logback
- Correlación con trace IDs
- Agregación centralizada

## Seguridad

### Autenticación y Autorización

- JWT tokens con refresh mechanism
- Role-based access control (RBAC)
- OAuth2 para integraciones externas

### Secretos

- HashiCorp Vault para gestión centralizada
- Rotación automática de credenciales
- Cifrado en tránsito y reposo

### QR Code Security

- Firma digital de códigos QR
- Validación de integridad
- Prevención de falsificación

## Escalabilidad

### Horizontal Scaling

- Servicios stateless
- Load balancing con NGINX
- Auto-scaling en Kubernetes

### Database Scaling

- Read replicas para consultas
- Particionamiento por estación
- Connection pooling

### Caching Strategy

- Redis para cache distribuido
- Cache-aside pattern
- TTL configurables por tipo de dato

## Deployment

### Containerización

- Docker para todos los servicios
- Multi-stage builds para optimización
- Health checks integrados

### Orchestración

- Kubernetes para producción
- Helm charts para gestión
- GitOps con ArgoCD

### CI/CD

- GitHub Actions para pipelines
- Automated testing y security scans
- Blue-green deployments
