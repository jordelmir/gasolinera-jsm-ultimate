# 🎮 Gasolinera JSM - Sistema de Cupones Digitales Gamificado

Una plataforma revolucionaria que transforma los cupones físicos tradicionales en una experiencia digital gamificada con sorteos semanales y anuales.

## 🎯 Propósito

Digitalizar completamente el sistema de cupones de gasolineras, reemplazando los cupones físicos por códigos QR únicos que los clientes pueden escanear para participar en sorteos. Cada ₡5,000 de compra = 1 ticket para sorteos semanales de ₡40,000 y sorteo anual de un carro.

## ✨ Características Principales

### 👤 Para Clientes

- **Dashboard Personal**: Visualiza tus tickets acumulados
- **Escáner QR**: Escanea códigos del dispensador
- **Sistema de Anuncios Gamificado**: Ve anuncios para duplicar tickets (10s → 15s → 30s → 1m → hasta 10m)
- **Tokens Únicos**: Cada cupón genera un token único para sorteos
- **Celebración de Ganadores**: Pantallas especiales para ganadores

### ⛽ Para Empleados/Dispensadores

- **Interfaz Ultra-Simple**: Contador +/- para múltiplos de ₡5,000
- **Generador QR**: Botón que genera códigos únicos por transacción
- **Reset Automático**: Listo para el siguiente cliente

### 🏢 Para Dueños/Administradores

- **Dashboard Ejecutivo**: Métricas avanzadas y KPIs
- **Gestión de Estaciones**: Administra múltiples gasolineras
- **Gestión de Empleados**: Registra y asigna personal
- **Analytics Detallados**: Rendimiento por empleado y sucursal
- **Control de Sorteos**: Gestión de premios y ganadores

## Arquitectura y Stack Tecnológico

El sistema está construido sobre una arquitectura de microservicios utilizando un monorepo `nx`.

- **Backend:** Kotlin + Spring Boot 3 + Java 17
- **Frontend (Web):** Next.js + TypeScript + Tailwind CSS
- **Móvil:** React Native (Expo) + TypeScript
- **Base de Datos:** PostgreSQL
- **Cache/Locks:** Redis
- **Mensajería:** RabbitMQ (con soporte para Kafka a través de Debezium)
- **Observabilidad:** OpenTelemetry y Jaeger para tracing distribuido.
- **Patrones de Diseño:** Patrón Outbox con Debezium para garantizar la entrega de mensajes.
- **API y SDK:** OpenAPI para la definición de APIs y generación de un SDK interno para la comunicación entre servicios.
- **Infraestructura:** Docker, Kubernetes (Helm), Terraform
- **CI/CD:** GitHub Actions

## 🚀 Quick Start

### Requisitos Previos

- **Docker Desktop** - Para containerización
- **Node.js 18+** - Para aplicaciones frontend
- **JDK 17** - Para servicios backend
- **Git** - Control de versiones

### Setup en 3 Pasos

```bash
# 1. Clonar y configurar
git clone https://github.com/jordelmir/gasolinera-jsm-ultimate.git
cd gasolinera-jsm-ultimate
cp .env.example .env

# 2. Instalar dependencias
npm install

# 3. Levantar el entorno completo
make dev
```

### Verificación

```bash
# Ver logs de todos los servicios
make logs

# Verificar que todo esté funcionando
curl http://localhost:8080/actuator/health
```

### Desarrollo Frontend Únicamente

Si solo quieres trabajar en el frontend:

```bash
make dev-frontend
```

## Servicios y URLs Locales

Una vez que `make dev` se complete, los siguientes servicios estarán disponibles:

- **API Gateway:** [http://localhost:8080](http://localhost:8080)
- **Admin Dashboard:** [http://localhost:3000](http://localhost:3000)
- **Advertiser Portal:** [http://localhost:3001](http://localhost:3001)
- **PostgreSQL:** `localhost:5432`
- **Redis:** `localhost:6379`
- **RabbitMQ Management:** [http://localhost:15672](http://localhost:15672)
- **Jaeger UI (Tracing):** [http://localhost:16686](http://localhost:16686)

## Scripts Útiles

- `make build-all`: Construye las imágenes Docker de todos los servicios.
- `make dev`: Inicia todo el entorno de desarrollo con Docker Compose.
- `make stop`: Detiene todos los contenedores.
- `make clean`: Detiene y elimina todos los contenedores, volúmenes y redes.
- `make logs`: Muestra los logs de todos los servicios.
- `make test`: Ejecuta tests unitarios e de integración en todos los servicios.
- `make seed`: Ejecuta el script de seeding para poblar la base de datos con datos de prueba.
- `make mobile`: Inicia el servidor de desarrollo de la app móvil (Expo).
- `make k8s-up`: Despliega la aplicación en un clúster de Kubernetes local.
- `make k8s-down`: Elimina el despliegue de Kubernetes.

## FAQ

**¿Cómo genero nuevos códigos QR firmados?**
Usa el script de `ops`. Requiere que el entorno esté corriendo para acceder a los secretos.

```bash
npm run nx -- run ops:qr:generate --count 10
```

**¿Dónde están las credenciales de prueba?**
Revisa el script de seeding `ops/scripts/dev/seed.ts` para ver los usuarios y estaciones de prueba que se crean.

## 📊 Métricas y Monitoreo

Una vez que el sistema esté ejecutándose, puedes acceder a:

- **Jaeger Tracing**: [http://localhost:16686](http://localhost:16686) - Trazabilidad distribuida
- **RabbitMQ Management**: [http://localhost:15672](http://localhost:15672) - Gestión de colas
- **Health Checks**: `http://localhost:8080/actuator/health` - Estado de servicios

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Por favor lee nuestra [Guía de Contribución](CONTRIBUTING.md) para conocer el proceso.

### Desarrollo Local

1. Fork el repositorio
2. Crea una rama feature (`git checkout -b feature/amazing-feature`)
3. Commit tus cambios (`git commit -m 'feat: add amazing feature'`)
4. Push a la rama (`git push origin feature/amazing-feature`)
5. Abre un Pull Request

## 📚 Documentación Adicional

- [Arquitectura del Sistema](docs/ARCHITECTURE.md)
- [Guía de Contribución](CONTRIBUTING.md)
- [API Documentation](docs/API.md)

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 🆘 Soporte

Si encuentras algún problema o tienes preguntas:

- Crea un [issue](https://github.com/jordelmir/gasolinera-jsm-ultimate/issues)
- Revisa la documentación en `/docs`
- Contacta al equipo de desarrollo

## 🌐 Servicios y URLs Locales

Una vez que `make dev` se complete, los siguientes servicios estarán disponibles:

### Backend Services

- **API Gateway:** [http://localhost:8080](http://localhost:8080)
- **Auth Service:** `localhost:8081`
- **Coupon Service:** `localhost:8086`
- **Station Service:** `localhost:8083`
- **Ad Engine:** `localhost:8084`
- **Raffle Service:** `localhost:8085`
- **Redemption Service:** `localhost:8082`

### Frontend Applications

- **Owner Dashboard:** [http://localhost:3002](http://localhost:3002)
- **Admin Panel:** [http://localhost:3000](http://localhost:3000)
- **Advertiser Portal:** [http://localhost:3001](http://localhost:3001)

### Infrastructure

- **PostgreSQL:** `localhost:5432`
- **Redis:** `localhost:6379`
- **RabbitMQ Management:** [http://localhost:15672](http://localhost:15672) (user/password)
- **Jaeger Tracing:** [http://localhost:16686](http://localhost:16686)
- **Vault:** [http://localhost:8200](http://localhost:8200) (token: myroottoken)

### Mobile Apps

- **Cliente Mobile:** `expo start` en `apps/client-mobile/`
- **Empleado Mobile:** `expo start` en `apps/employee-mobile/`

## 🎮 Cómo Funciona el Sistema

### Para Clientes

1. **Descargar la app móvil** de cliente
2. **Registrarse** con email y teléfono
3. **Escanear QR** generado por el empleado en la gasolinera
4. **Activar cupón** y ver anuncios para duplicar tickets
5. **Participar automáticamente** en sorteos semanales y anuales

### Para Empleados

1. **Usar la app móvil** de empleado
2. **Seleccionar monto** de la compra (múltiplos de ₡5,000)
3. **Generar QR** único para el cliente
4. **Mostrar QR** al cliente para escanear

### Para Dueños

1. **Acceder al dashboard web** de administración
2. **Gestionar estaciones** y empleados
3. **Ver analytics** y métricas de rendimiento
4. **Configurar sorteos** y premios

## 🎯 Flujo de Cupones Digitales

```
Cliente compra ₡15,000 → Empleado genera QR (3 tickets base)
↓
Cliente escanea QR → Activa cupón
↓
Ve anuncio de 10s → Duplica tickets (6 total)
↓
Ve anuncio de 15s → Duplica tickets (12 total)
↓
Continúa hasta 10 anuncios máximo → Máximo 3,072 tickets
↓
Participa automáticamente en sorteos
```

## 🏆 Sistema de Sorteos

- **Sorteo Semanal:** ₡40,000 cada domingo
- **Sorteo Anual:** Un carro en diciembre
- **Algoritmo:** Completamente aleatorio y transparente
- **Elegibilidad:** Todos los tickets activos participan

## 🚀 Deployment en Producción

### Usando el script automatizado:

```bash
# Staging
./scripts/deploy.sh staging

# Producción
./scripts/deploy.sh production
```

### Manual:

```bash
# Configurar variables de entorno
cp .env.production .env

# Deploy con Docker Compose
make deploy-production

# Verificar deployment
curl https://api.gasolinera-jsm.com/actuator/health
```

## 🔧 Comandos de Desarrollo

### Desarrollo Completo

```bash
make dev                    # Todo el sistema
make dev-mobile            # Solo apps móviles
make dev-web               # Solo apps web
```

### Apps Individuales

```bash
make client-mobile         # App cliente
make employee-mobile       # App empleado
make owner-dashboard       # Dashboard dueño
```

### Base de Datos

```bash
make seed-coupon-system    # Datos de prueba
make db-backup            # Backup
make db-restore           # Restaurar
```

## 🔐 Credenciales de Prueba

Después de ejecutar `make seed-coupon-system`:

- **Cliente:** `cliente@test.com` / `password123`
- **Empleado:** `empleado@test.com` / `password123`
- **Dueño:** `dueno@test.com` / `password123`
- **Anunciante:** `anunciante@test.com` / `password123`

## 🏗️ Arquitectura Técnica

### Microservicios

- **Coupon Service:** Gestión de QR y cupones
- **Auth Service:** Autenticación y autorización
- **Station Service:** Gestión de estaciones
- **Ad Engine:** Motor de anuncios y secuencias
- **Raffle Service:** Sistema de sorteos
- **Redemption Service:** Canjes y recompensas

### Patrones Implementados

- **Event Sourcing:** Para trazabilidad completa
- **CQRS:** Separación de comandos y consultas
- **Circuit Breaker:** Resiliencia entre servicios
- **Outbox Pattern:** Consistencia eventual

### Seguridad

- **JWT Tokens:** Autenticación stateless
- **QR Firmados:** Prevención de falsificación
- **Rate Limiting:** Protección contra abuso
- **HTTPS/TLS:** Cifrado en tránsito

## 📱 Apps Móviles

### Cliente (React Native + Expo)

- Escáner QR integrado
- Sistema de anuncios gamificado
- Dashboard personal de tickets
- Notificaciones push para sorteos

### Empleado (React Native + Expo)

- Interfaz ultra-simple
- Generador QR dinámico
- Estadísticas en tiempo real
- Modo offline básico

## 🌟 Características Avanzadas

### Gamificación

- Anuncios progresivos (10s → 10min)
- Multiplicadores de tickets
- Sistema de logros
- Pantallas de celebración

### Analytics

- Métricas en tiempo real
- Dashboards interactivos
- Reportes automatizados
- Segmentación de usuarios

### Escalabilidad

- Arquitectura de microservicios
- Cache distribuido con Redis
- Load balancing con Nginx
- Auto-scaling en Kubernetes
