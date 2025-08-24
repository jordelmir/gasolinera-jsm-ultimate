# Auth Service

Servicio de autenticación para la plataforma Gasolinera JSM. Maneja la autenticación basada en OTP para usuarios móviles y autenticación tradicional para administradores y anunciantes.

## Arquitectura

Este servicio implementa **Arquitectura Hexagonal (Ports & Adapters)** para mantener la lógica de negocio independiente de los detalles de infraestructura.

### Estructura del Proyecto

```
src/main/kotlin/com/gasolinerajsm/authservice/
├── domain/                     # Entidades de dominio y reglas de negocio
│   ├── User.kt                # Entidad principal de usuario
│   └── ports/                 # Interfaces (puertos)
│       ├── UserRepository.kt
│       ├── OtpService.kt
│       └── TokenService.kt
├── application/               # Casos de uso (lógica de aplicación)
│   └── AuthenticationUseCase.kt
├── infrastructure/           # Adaptadores de infraestructura
│   ├── adapters/            # Implementaciones de puertos
│   ├── entities/            # Entidades JPA
│   └── repositories/        # Repositorios Spring Data
├── controller/              # Controladores REST (adaptadores de entrada)
├── dto/                    # Objetos de transferencia de datos
└── service/               # Servicios legacy (a refactorizar)
```

## Funcionalidades

### Autenticación por OTP

- Generación de códigos OTP de 6 dígitos
- Almacenamiento temporal en Redis (5 minutos)
- Verificación y generación de tokens JWT
- Creación automática de usuarios

### Autenticación de Administradores

- Login con email/password para administradores
- Login con email/password para anunciantes
- Generación de tokens JWT con roles específicos

### Gestión de Tokens

- Tokens de acceso (1 hora de duración)
- Tokens de refresco (7 días de duración)
- Validación y verificación de tokens

## Configuración

### Variables de Entorno

```yaml
# Base de datos
SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/gasolinera_dev
SPRING_DATASOURCE_USERNAME: dev_user
SPRING_DATASOURCE_PASSWORD: dev_password

# Redis
SPRING_DATA_REDIS_HOST: localhost
SPRING_DATA_REDIS_PORT: 6379

# JWT
APP_JWT_SECRET: your-secret-key-here
APP_JWT_ACCESS_TOKEN_EXPIRATION: 3600000 # 1 hora
APP_JWT_REFRESH_TOKEN_EXPIRATION: 604800000 # 7 días

# Credenciales de administrador (temporal)
APP_AUTH_ADMIN_EMAIL: admin@puntog.com
APP_AUTH_ADMIN_PASSWORD: admin123
APP_AUTH_ADVERTISER_EMAIL: anunciante@tosty.com
APP_AUTH_ADVERTISER_PASSWORD: tosty123
```

## Desarrollo

### Prerrequisitos

- Java 17+
- PostgreSQL 15+
- Redis 7+

### Ejecutar Localmente

```bash
# Desde la raíz del proyecto
./gradlew :auth-service:bootRun

# O usando Docker Compose
docker-compose -f docker-compose.dev.yml up auth-service
```

### Ejecutar Tests

```bash
./gradlew :auth-service:test
```

### Generar Documentación OpenAPI

```bash
./gradlew :auth-service:generateOpenApiDocs
```

## API Endpoints

### Autenticación por OTP

#### Solicitar OTP

```http
POST /auth/otp/request
Content-Type: application/json

{
  "phone": "+50612345678"
}
```

#### Verificar OTP

```http
POST /auth/otp/verify
Content-Type: application/json

{
  "phone": "+50612345678",
  "code": "123456"
}
```

### Autenticación de Administradores

#### Login de Administrador

```http
POST /auth/login/admin
Content-Type: application/json

{
  "email": "admin@puntog.com",
  "pass": "admin123"
}
```

#### Login de Anunciante

```http
POST /auth/login/advertiser
Content-Type: application/json

{
  "email": "anunciante@tosty.com",
  "pass": "tosty123"
}
```

## Monitoreo

### Health Check

```http
GET /actuator/health
```

### Métricas

```http
GET /actuator/prometheus
```

## Próximos Pasos

1. **Migrar servicios legacy** - Refactorizar `JwtService` y `UserService` para usar la nueva arquitectura
2. **Implementar SMS real** - Integrar con proveedor de SMS para envío de OTP
3. **Mejorar seguridad** - Implementar rate limiting y validaciones adicionales
4. **Tests de integración** - Añadir tests completos con TestContainers
5. **Gestión de usuarios** - Implementar CRUD completo de usuarios y roles

## Contribución

1. Seguir las convenciones de arquitectura hexagonal
2. Mantener la separación entre dominio, aplicación e infraestructura
3. Escribir tests para todos los casos de uso
4. Documentar cambios en la API con OpenAPI
