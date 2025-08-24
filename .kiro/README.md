# KiroIDE Configuration

Este directorio contiene la configuración personalizada de Kiro IDE para el proyecto Gasolinera JSM.

## Archivos de Configuración

### `settings/`

- **`kiro.json`** - Configuración principal de Kiro IDE
- **`shell.json`** - Configuración del entorno de shell
- **`terminal.json`** - Perfiles y configuración del terminal
- **`workspace.json`** - Configuración específica del workspace
- **`mcp.json`** - Configuración de servidores MCP

### `scripts/`

- **`init-environment.sh`** - Script de inicialización del entorno de desarrollo

### `specs/`

- Directorio para especificaciones de features (specs)

### `steering/`

- Directorio para reglas de steering (guías de desarrollo)

## Configuración del Shell Environment

### Timeout de Resolución

El timeout de resolución del entorno de shell está configurado a **30 segundos** para permitir que todos los servicios y herramientas se inicialicen correctamente.

### Variables de Entorno

- `NODE_ENV=development`
- `JAVA_HOME` - Configurado para Java 17
- `GRADLE_OPTS` - Optimizado para el proyecto
- `NX_DAEMON=true` - Habilita el daemon de Nx
- `DOCKER_BUILDKIT=1` - Habilita BuildKit de Docker

### Aliases Útiles

- `nx` - Acceso directo a Nx CLI
- `gradle` - Usa ./gradlew del proyecto
- `dc` - Docker Compose
- `dcd` - Docker Compose con archivo dev
- `dcl` - Ver logs de Docker Compose
- `dcs` - Estado de contenedores

## Perfiles de Terminal

### `development` (por defecto)

Entorno completo de desarrollo con todas las herramientas disponibles.

### `backend`

Enfocado en desarrollo de servicios Kotlin/Spring Boot.

### `frontend`

Enfocado en desarrollo de aplicaciones Next.js/React.

### `docker`

Enfocado en gestión de contenedores y servicios.

## Comandos Rápidos

- **Start Development**: `make dev`
- **Stop All Services**: `make stop`
- **View Logs**: `make logs`
- **Run Tests**: `make test`
- **Clean Environment**: `make clean`
- **Seed Database**: `make seed`

## Puertos de Servicios

### Backend Services

- API Gateway: 8080
- Auth Service: 8081
- Redemption Service: 8082
- Station Service: 8083
- Ad Engine: 8084
- Raffle Service: 8085
- Coupon Service: 8086

### Frontend Applications

- Admin Dashboard: 3000
- Advertiser Portal: 3001
- Owner Dashboard: 3002

### Infrastructure

- PostgreSQL: 5432
- Redis: 6379
- RabbitMQ: 5672 (Management: 15672)
- Jaeger UI: 16686

## Inicialización Automática

El entorno se inicializa automáticamente cuando abres Kiro IDE. Si necesitas reinicializar manualmente:

```bash
./.kiro/scripts/init-environment.sh
```

## MCP Servers

Todos los servidores MCP están configurados y habilitados para proporcionar funcionalidades extendidas:

- **Core**: fetch, filesystem, shell, git, docker
- **Databases**: postgres, sqlite, mongodb, redis
- **Cloud**: aws-docs, kubernetes
- **Productivity**: memory, time, notion
- **Development**: github, puppeteer, brave-search

## Troubleshooting

### Shell Environment Resolution Timeout

Si experimentas timeouts al resolver el entorno de shell:

1. Verifica que todas las herramientas estén instaladas (Node.js, Java, Docker)
2. Aumenta el timeout en `settings/kiro.json` si es necesario
3. Ejecuta manualmente el script de inicialización

### MCP Server Issues

Si algún servidor MCP no funciona:

1. Verifica que `uvx` esté instalado
2. Revisa los logs en la vista de MCP Servers
3. Reconecta los servidores desde el panel de Kiro

## Personalización

Puedes personalizar cualquier configuración editando los archivos JSON correspondientes. Los cambios se aplicarán automáticamente al reiniciar Kiro IDE.
