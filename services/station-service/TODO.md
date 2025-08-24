# Station Service - TODO y Mejoras

## ✅ Estado Actual

- **Compilación**: ✅ Exitosa
- **Estructura**: ✅ Bien organizada
- **Documentación**: ✅ OpenAPI configurado
- **Tests**: ⚠️ Básicos presentes

## 🔧 Mejoras Requeridas

### 1. Arquitectura y Código

#### 1.1 Implementar Arquitectura Hexagonal

- [ ] **Crear capa de dominio**
  - Mover `Station` a `domain/Station.kt`
  - Crear value objects para `StationId`, `Coordinates`, `StationStatus`
  - Definir reglas de negocio en el dominio

- [ ] **Crear puertos (interfaces)**
  - `StationRepository` port en `domain/ports/`
  - `LocationService` port para validación de coordenadas
  - `NotificationService` port para eventos de estación

- [ ] **Refactorizar adaptadores**
  - Mover `StationRepository` a `infrastructure/adapters/`
  - Crear `JpaStationRepository` como adaptador
  - Separar entidad JPA del modelo de dominio

#### 1.2 Mejorar Validaciones

- [ ] **Validaciones de negocio**
  - Validar que las coordenadas sean válidas para Costa Rica
  - Verificar que no existan estaciones duplicadas en la misma ubicación
  - Validar nombres únicos de estaciones

- [ ] **Manejo de errores**
  - Crear excepciones específicas del dominio
  - Mejorar `GlobalExceptionHandler` con más casos
  - Añadir logging estructurado

### 2. Funcionalidades Faltantes

#### 2.1 Gestión de Empleados

- [ ] **Modelo de empleados**

  ```kotlin
  data class Employee(
      val id: EmployeeId,
      val stationId: StationId,
      val name: String,
      val role: EmployeeRole,
      val isActive: Boolean
  )
  ```

- [ ] **Endpoints de empleados**
  - `GET /api/v1/stations/{id}/employees`
  - `POST /api/v1/stations/{id}/employees`
  - `PUT /api/v1/employees/{id}`
  - `DELETE /api/v1/employees/{id}`

#### 2.2 Gestión de Dispensadores

- [ ] **Modelo de dispensadores**

  ```kotlin
  data class Dispenser(
      val id: DispenserId,
      val stationId: StationId,
      val number: Int,
      val fuelType: FuelType,
      val status: DispenserStatus
  )
  ```

- [ ] **Endpoints de dispensadores**
  - `GET /api/v1/stations/{id}/dispensers`
  - `POST /api/v1/stations/{id}/dispensers`
  - `PUT /api/v1/dispensers/{id}/status`

#### 2.3 Estadísticas y Reportes

- [ ] **Métricas por estación**
  - Cupones generados por período
  - Empleados activos
  - Dispensadores operativos
  - Ingresos estimados

- [ ] **Endpoints de estadísticas**
  - `GET /api/v1/stations/{id}/stats`
  - `GET /api/v1/stations/{id}/performance`

### 3. Integración y Comunicación

#### 3.1 Eventos de Dominio

- [ ] **Implementar eventos**
  - `StationCreated`
  - `StationStatusChanged`
  - `EmployeeAdded`
  - `DispenserStatusChanged`

- [ ] **Integración con RabbitMQ**
  - Configurar publisher de eventos
  - Crear listeners para eventos externos

#### 3.2 Integración con Otros Servicios

- [ ] **Auth Service**
  - Validar tokens de empleados y propietarios
  - Verificar permisos por estación

- [ ] **Coupon Service**
  - Validar que la estación existe al generar cupones
  - Proporcionar información de empleados

### 4. Persistencia y Migraciones

#### 4.1 Mejorar Modelo de Datos

- [ ] **Tabla stations mejorada**

  ```sql
  CREATE TABLE stations (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      name VARCHAR(255) NOT NULL,
      address TEXT,
      latitude DECIMAL(10, 8) NOT NULL,
      longitude DECIMAL(11, 8) NOT NULL,
      status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
      owner_id UUID,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );
  ```

- [ ] **Tabla employees**

  ```sql
  CREATE TABLE employees (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      station_id UUID NOT NULL REFERENCES stations(id),
      name VARCHAR(255) NOT NULL,
      email VARCHAR(255),
      role VARCHAR(50) NOT NULL,
      is_active BOOLEAN DEFAULT true,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );
  ```

- [ ] **Tabla dispensers**
  ```sql
  CREATE TABLE dispensers (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      station_id UUID NOT NULL REFERENCES stations(id),
      number INTEGER NOT NULL,
      fuel_type VARCHAR(50) NOT NULL,
      status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );
  ```

#### 4.2 Migraciones Flyway

- [ ] **Crear migraciones**
  - `V2__Add_address_and_owner_to_stations.sql`
  - `V3__Create_employees_table.sql`
  - `V4__Create_dispensers_table.sql`

### 5. Testing

#### 5.1 Tests Unitarios

- [ ] **Domain tests**
  - Validaciones de value objects
  - Reglas de negocio
  - Casos edge

- [ ] **Service tests**
  - Mocks de repositorios
  - Casos de error
  - Validaciones

#### 5.2 Tests de Integración

- [ ] **Repository tests**
  - TestContainers con PostgreSQL
  - Queries complejas
  - Transacciones

- [ ] **Controller tests**
  - MockMvc tests
  - Validación de DTOs
  - Manejo de errores

### 6. Configuración y Deployment

#### 6.1 Configuración

- [ ] **Profiles específicos**
  - `application-development.yml`
  - `application-staging.yml`
  - `application-production.yml`

- [ ] **Variables de entorno**
  - Database connection
  - External service URLs
  - Feature flags

#### 6.2 Dockerfile

- [ ] **Crear Dockerfile optimizado**
  ```dockerfile
  FROM openjdk:17-jre-slim
  COPY build/libs/station-service.jar app.jar
  EXPOSE 8083
  ENTRYPOINT ["java", "-jar", "/app.jar"]
  ```

### 7. Monitoreo y Observabilidad

#### 7.1 Métricas Personalizadas

- [ ] **Business metrics**
  - `stations.total`
  - `employees.active.total`
  - `dispensers.operational.total`

#### 7.2 Health Checks

- [ ] **Custom health indicators**
  - Database connectivity
  - External service availability

## 🚀 Prioridades de Implementación

### Fase 1 (Crítica)

1. Implementar arquitectura hexagonal básica
2. Mejorar validaciones y manejo de errores
3. Crear migraciones de base de datos

### Fase 2 (Importante)

1. Añadir gestión de empleados
2. Implementar eventos de dominio
3. Crear tests de integración

### Fase 3 (Deseable)

1. Gestión de dispensadores
2. Estadísticas avanzadas
3. Métricas personalizadas

## 📝 Notas de Implementación

- Mantener compatibilidad con APIs existentes
- Usar UUID para todos los IDs
- Implementar soft deletes para auditoría
- Considerar cache para consultas frecuentes
- Documentar todos los cambios en OpenAPI
