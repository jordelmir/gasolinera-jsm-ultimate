# Guía de Contribución - Gasolinera JSM

## Configuración del Entorno de Desarrollo

### Prerrequisitos

- Docker Desktop
- Node.js 18+
- JDK 17
- Git

### Setup Inicial

```bash
# Clonar el repositorio
git clone https://github.com/jordelmir/gasolinera-jsm-ultimate.git
cd gasolinera-jsm-ultimate

# Instalar dependencias
npm install

# Configurar variables de entorno
cp .env.example .env

# Levantar servicios de desarrollo
make dev
```

## Estructura del Proyecto

```
gasolinera-jsm-ultimate/
├── apps/                    # Aplicaciones frontend
│   ├── admin/              # Dashboard administrativo
│   ├── advertiser/         # Portal de anunciantes
│   └── mobile/             # App móvil React Native
├── services/               # Microservicios backend
│   ├── api-gateway/        # Gateway principal
│   ├── auth-service/       # Servicio de autenticación
│   ├── station-service/    # Gestión de estaciones
│   ├── redemption-service/ # Sistema de canjes
│   ├── ad-engine/          # Motor de publicidad
│   └── raffle-service/     # Sistema de sorteos
├── packages/               # Librerías compartidas
├── infra/                  # Infraestructura como código
├── ops/                    # Scripts operacionales
└── docs/                   # Documentación
```

## Flujo de Desarrollo

### Branching Strategy

- `main`: Rama principal (producción)
- `develop`: Rama de desarrollo
- `feature/*`: Nuevas funcionalidades
- `bugfix/*`: Corrección de errores
- `hotfix/*`: Correcciones urgentes

### Proceso de Desarrollo

1. Crear rama desde `develop`
2. Implementar cambios
3. Ejecutar tests localmente
4. Crear Pull Request
5. Code review
6. Merge a `develop`

## Estándares de Código

### Frontend (TypeScript/React)

```typescript
// Usar interfaces para props
interface ComponentProps {
  title: string;
  isVisible?: boolean;
}

// Componentes funcionales con hooks
const Component: React.FC<ComponentProps> = ({ title, isVisible = true }) => {
  const [state, setState] = useState<string>('');

  return (
    <div className="container mx-auto">{isVisible && <h1>{title}</h1>}</div>
  );
};
```

### Backend (Kotlin/Spring Boot)

```kotlin
// Usar data classes para DTOs
data class UserDto(
    val id: Long,
    val email: String,
    val name: String
)

// Servicios con inyección de dependencias
@Service
class UserService(
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun createUser(request: CreateUserRequest): UserDto {
        // Implementación
    }
}
```

## Testing

### Frontend Tests

```bash
# Ejecutar tests de un proyecto específico
npm run nx -- test admin

# Tests con coverage
npm run nx -- test admin --coverage

# Tests en modo watch
npm run nx -- test admin --watch
```

### Backend Tests

```bash
# Tests unitarios
./gradlew test

# Tests de integración
./gradlew integrationTest

# Todos los tests
make test
```

## Comandos Útiles

### Desarrollo

```bash
make dev              # Levantar entorno completo
make dev-frontend     # Solo frontends en modo dev
make logs             # Ver logs de todos los servicios
make stop             # Parar servicios
make clean            # Limpiar completamente
```

### Calidad de Código

```bash
make lint             # Linting
make format           # Formatear código
make check-deps       # Verificar dependencias
```

### Base de Datos

```bash
make seed             # Poblar con datos de prueba
```

## Convenciones

### Commits

Usar [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add user authentication
fix: resolve memory leak in ad-engine
docs: update API documentation
test: add integration tests for redemption service
```

### Pull Requests

- Título descriptivo
- Descripción detallada de cambios
- Referencias a issues relacionados
- Screenshots para cambios de UI
- Tests que cubran los cambios

### Naming Conventions

- **Branches**: `feature/user-authentication`, `bugfix/memory-leak`
- **Components**: `PascalCase` (UserProfile.tsx)
- **Functions**: `camelCase` (getUserById)
- **Constants**: `UPPER_SNAKE_CASE` (API_BASE_URL)
- **Files**: `kebab-case` (user-service.ts)

## Debugging

### Frontend

```bash
# Debug con Chrome DevTools
npm run nx -- serve admin --inspect

# Analizar bundle
npm run nx -- build admin --analyze
```

### Backend

```bash
# Debug con puerto 5005
./gradlew bootRun --debug-jvm

# Profiling con JProfiler
./gradlew bootRun -Dspring.profiles.active=profiling
```

## Deployment

### Staging

```bash
# Deploy a staging
git push origin develop
# GitHub Actions se encarga del resto
```

### Producción

```bash
# Crear release
git checkout main
git merge develop
git tag v1.0.0
git push origin main --tags
```

## Recursos Adicionales

- [Documentación de Arquitectura](docs/ARCHITECTURE.md)
- [API Documentation](docs/API.md)
- [Troubleshooting Guide](docs/TROUBLESHOOTING.md)
- [Performance Guidelines](docs/PERFORMANCE.md)

## Contacto

Para preguntas o soporte:

- Crear issue en GitHub
- Slack: #gasolinera-jsm-dev
- Email: dev-team@gasolinera-jsm.com
