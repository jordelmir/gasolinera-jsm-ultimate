# Station Service

Servicio de gestión de estaciones de gasolina para la plataforma Gasolinera JSM. Maneja el registro, actualización y consulta de estaciones, empleados y dispensadores.

## Funcionalidades

### Gestión de Estaciones

- **CRUD completo**: Crear, leer, actualizar y eliminar estaciones
- **Geolocalización**: Almacenamiento de coordenadas GPS
- **Estados**: Control de estado operativo (ACTIVE, INACTIVE, MAINTENANCE)
- **Validaciones**: Verificación de coordenadas y datos requeridos

### Próximas Funcionalidades

- **Gestión de empleados** por estación
- **Control de dispensadores** y su estado
- **Estadísticas** de operación por estación
- **Integración** con otros servicios del ecosistema

## Arquitectura

### Estructura Actual

```
src/main/kotlin/com/gasolinerajsm/stationservice/
├── controller/              # Controladores REST
│   └── StationController.kt
├── dto/                    # Objetos de transferencia
│   └── StationDto.kt
├── model/                  # Entidades JPA
│   └── Station.kt
├── service/                # Lógica de negocio
│   └── StationService.kt
├── repository/             # Acceso a datos
│   └── StationRepository.kt
└── exception/              # Manejo de errores
    └── GlobalExceptionHandler.kt
```

### Migración a Arquitectura Hexagonal (Planificada)

```
src/main/kotlin/com/gasolinerajsm/stationservice/
├── domain/                 # Entidades de dominio y reglas
├── application/            # Casos de uso
├── infrastructure/         # Adaptadores (JPA, REST, etc.)
└── controller/            # Adaptadores de entrada
```

## API Endpoints

### Gestión de Estaciones

#### Listar todas las estaciones

```http
GET /api/v1/stations
```

**Respuesta:**

```json
[
  {
    "id": "uuid",
    "name": "Estación Central",
    "latitude": 9.9281,
    "longitude": -84.0907,
    "status": "ACTIVE"
  }
]
```

#### Obtener estación por ID

```http
GET /api/v1/stations/{id}
```

#### Crear nueva estación

```http
POST /api/v1/stations
Content-Type: application/json

{
  "name": "Nueva Estación",
  "latitude": 9.9281,
  "longitude": -84.0907,
  "status": "ACTIVE"
}
```

#### Actualizar estación

```http
PUT /api/v1/stations/{id}
Content-Type: application/json

{
  "name": "Estación Actualizada",
  "latitude": 9.9281,
  "longitude": -84.0907,
  "status": "MAINTENANCE"
}
```

#### Eliminar estación

```http
DELETE /api/v1/stations/{id}
```

## Configuración

### Variables de Entorno

```yaml
# Base de datos
SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/gasolinera_dev
SPRING_DATASOURCE_USERNAME: dev_user
SPRING_DATASOURCE_PASSWORD: dev_password

# Flyway migrations
SPRING_FLYWAY_ENABLED: true
SPRING_FLYWAY_LOCATIONS: classpath:db/migration

# Actuator
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE: health,info,prometheus
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS: always
```

### Perfiles de Configuración

- **development**: Configuración para desarrollo local
- **staging**: Configuración para ambiente de pruebas
- **production**: Configuración para producción

## Desarrollo

### Prerrequisitos

- Java 17+
- PostgreSQL 15+
- Gradle 8+

### Ejecutar Localmente

```bash
# Desde la raíz del proyecto
./gradlew :services:station-service:bootRun

# O usando Docker Compose
docker-compose -f docker-compose.dev.yml up station-service
```

### Ejecutar Tests

```bash
./gradlew :services:station-service:test
```

### Generar Documentación OpenAPI

```bash
./gradlew :services:station-service:generateOpenApiDocs
```

La documentación estará disponible en: `http://localhost:8083/swagger-ui.html`

## Base de Datos

### Esquema Actual

```sql
CREATE TABLE stations (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    status VARCHAR(255) NOT NULL
);
```

### Migraciones Flyway

Las migraciones se encuentran en `src/main/resources/db/migration/`:

- `V1__Create_stations_table.sql` - Tabla inicial de estaciones

## Validaciones

### Coordenadas GPS

- **Latitud**: Entre -90.0 y 90.0
- **Longitud**: Entre -180.0 y 180.0
- **Precisión**: Validación específica para Costa Rica (planificada)

### Datos Requeridos

- **Nombre**: No puede estar vacío
- **Estado**: Debe ser un valor válido (ACTIVE, INACTIVE, MAINTENANCE)

## Monitoreo

### Health Check

```http
GET /actuator/health
```

### Métricas

```http
GET /actuator/prometheus
```

### Información del Servicio

```http
GET /actuator/info
```

## Integración con Otros Servicios

### Coupon Service

- Valida existencia de estaciones al generar cupones
- Proporciona información de empleados (planificado)

### Auth Service

- Valida permisos de propietarios y empleados (planificado)
- Controla acceso por estación (planificado)

## Próximos Pasos

Ver [TODO.md](./TODO.md) para una lista detallada de mejoras planificadas:

1. **Arquitectura Hexagonal** - Refactorización completa
2. **Gestión de Empleados** - CRUD de empleados por estación
3. **Control de Dispensadores** - Gestión de bombas de combustible
4. **Estadísticas** - Métricas de operación y rendimiento
5. **Eventos de Dominio** - Integración asíncrona con otros servicios

## Contribución

1. Seguir las convenciones de código establecidas
2. Escribir tests para toda nueva funcionalidad
3. Actualizar documentación OpenAPI
4. Considerar impacto en otros servicios
5. Revisar el TODO.md antes de implementar cambios mayores

## Troubleshooting

### Problemas Comunes

1. **Error de conexión a PostgreSQL**

   ```bash
   docker-compose -f docker-compose.dev.yml up postgres
   ```

2. **Migraciones Flyway fallan**
   - Verificar que la base de datos esté limpia
   - Revisar logs de Flyway en startup

3. **Validación de coordenadas**
   - Verificar que los valores estén en el rango correcto
   - Usar formato decimal, no grados/minutos/segundos
