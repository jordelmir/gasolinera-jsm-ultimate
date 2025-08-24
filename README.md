# 🚀 Gasolinera JSM - Plataforma Digital de Cupones Gamificados

Una plataforma revolucionaria que transforma los cupones físicos de gasolineras en una experiencia digital gamificada con rifas semanales y anuales.

## 🎯 Propuesta de Valor

Digitalizar el sistema de cupones de gasolineras reemplazando cupones físicos con códigos QR únicos que los clientes escanean para participar en rifas. Cada compra de ₡5,000 = 1 boleto para rifas semanales de ₡40,000 y rifa anual de un automóvil.

## ✨ Características Principales

- **🎫 Sistema de Cupones Digitales**: Códigos QR únicos por transacción
- **📱 Aplicación Móvil**: Escaneo de QR y seguimiento de boletos
- **🎰 Sistema de Rifas**: Rifas semanales y anuales automatizadas
- **📺 Publicidad Gamificada**: Ver anuncios para multiplicar boletos (10s → 15s → 30s → 1m → hasta 10m)
- **👥 Multi-Usuario**: Clientes, empleados/dispensadores, y administradores
- **📊 Analytics Avanzados**: Dashboard ejecutivo con métricas y KPIs

## 🏗️ Arquitectura

### Microservicios (Spring Boot + Kotlin)

- **🔐 Auth Service** (8081) - Autenticación JWT con arquitectura hexagonal
- **🏪 Station Service** (8083) - Gestión de gasolineras y empleados
- **🎫 Coupon Service** (8086) - Generación y validación de cupones QR
- **🔄 Redemption Service** (8082) - Procesamiento de puntos y recompensas
- **📺 Ad Engine** (8084) - Servicio de anuncios y analytics
- **🎰 Raffle Service** (8085) - Sistema de rifas con transparencia blockchain
- **🌐 API Gateway** (8080) - Punto de entrada único con autenticación

### Infraestructura

- **🐘 PostgreSQL** - Base de datos principal
- **🔴 Redis** - Caché y sesiones
- **🐳 Docker** - Containerización
- **📊 Observabilidad** - OpenTelemetry + Jaeger + Prometheus

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 17+
- Docker & Docker Compose
- Gradle 8.8+

### Iniciar Entorno de Desarrollo

```bash
# Clonar repositorio
git clone <repository-url>
cd gasolinera-jsm-ultimate1111

# Iniciar todos los servicios
./start-dev.sh
```

### Servicios Disponibles

| Servicio           | URL                   | Descripción              |
| ------------------ | --------------------- | ------------------------ |
| Auth Service       | http://localhost:8081 | Autenticación y usuarios |
| Station Service    | http://localhost:8083 | Gestión de gasolineras   |
| Coupon Service     | http://localhost:8086 | Sistema de cupones       |
| Redemption Service | http://localhost:8082 | Procesamiento de puntos  |
| Ad Engine          | http://localhost:8084 | Motor de anuncios        |
| Raffle Service     | http://localhost:8085 | Sistema de rifas         |
| API Gateway        | http://localhost:8080 | Gateway principal        |

### Endpoints Útiles

```bash
# Health checks
curl http://localhost:8081/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8086/actuator/health

# API Documentation
open http://localhost:8081/swagger-ui.html
open http://localhost:8083/swagger-ui.html
open http://localhost:8086/swagger-ui.html
```

### Detener Servicios

```bash
./stop-dev.sh
```

## 🛠️ Desarrollo

### Compilar Todo

```bash
# Compilación completa
./gradlew build --parallel

# Solo compilación (sin tests)
./gradlew build --parallel -x test -x detekt
```

### Ejecutar Servicio Individual

```bash
# Auth Service
cd services/auth-service
SPRING_PROFILES_ACTIVE=development ../../gradlew bootRun

# Station Service
cd services/station-service
SPRING_PROFILES_ACTIVE=development ../../gradlew bootRun
```

### Generar Clientes SDK

```bash
# Generar todos los clientes
./gradlew generateAllClients

# Generar cliente específico
./gradlew generateAuthClient
./gradlew generateStationClient
```

## 📊 Estado del Proyecto

### ✅ Completado

- **Compilación**: 7/7 servicios (100%)
- **Arquitectura Hexagonal**: Auth Service completamente refactorizado
- **Documentación**: 3/7 servicios documentados
- **Configuración**: Docker, Gradle, dependencias optimizadas
- **Base de Datos**: PostgreSQL configurado con migraciones automáticas

### 🔄 En Progreso

- **Documentación**: README para servicios restantes
- **Tests**: Suite de tests unitarios e integración
- **API Gateway**: Implementación de seguridad JWT

### 📋 Próximos Pasos

1. Completar documentación de servicios faltantes
2. Implementar seguridad en API Gateway
3. Habilitar suite completa de tests
4. Refactorizar servicios restantes a arquitectura hexagonal

## 🏛️ Arquitectura de Servicios

### Auth Service ✅ (Arquitectura Hexagonal)

```
├── domain/           # Lógica de negocio pura
├── application/      # Casos de uso
├── infrastructure/   # Adaptadores (JPA, Redis, JWT)
└── controller/       # API REST
```

### Otros Servicios 🔄 (Arquitectura Tradicional → Hexagonal)

```
├── controller/       # API REST
├── service/          # Lógica de negocio
├── repository/       # Acceso a datos
└── model/           # Entidades
```

## 🔧 Comandos Útiles

### Desarrollo

```bash
# Ver servicios configurados
./gradlew listServices

# Verificar estado de clientes generados
./gradlew checkGeneratedClientsUpToDate

# Benchmark de generación de clientes
./gradlew benchmarkClientGeneration
```

### Base de Datos

```bash
# Conectar a PostgreSQL
docker exec -it gasolinera-postgres-dev psql -U dev_user -d auth_service_dev

# Ver logs de PostgreSQL
docker logs gasolinera-postgres-dev
```

### Monitoreo

```bash
# Ver logs de servicios
docker-compose -f docker-compose.dev.yml logs -f

# Métricas de Prometheus
curl http://localhost:8081/actuator/prometheus
```

## 📚 Documentación Adicional

- [Auth Service README](services/auth-service/README.md) ✅
- [Coupon Service README](services/coupon-service/README.md) ✅
- [Station Service README](services/station-service/README.md) ✅
- [API Gateway TODO](services/api-gateway/TODO.md)
- [Redemption Service TODO](services/redemption-service/TODO.md)
- [Ad Engine TODO](services/ad-engine/TODO.md)
- [Raffle Service TODO](services/raffle-service/TODO.md)

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama de feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

---

**Desarrollado con ❤️ por el equipo de Gasolinera JSM**

_Transformando la experiencia de combustible en Costa Rica_ 🇨🇷
