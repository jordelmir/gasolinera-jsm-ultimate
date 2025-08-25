# Testing Guide - Gasolinera JSM Ultimate

## Estructura de Testing

### Backend Testing (Kotlin/Spring Boot)
- **Unit Tests**: Tests unitarios para lógica de negocio
- **Integration Tests**: Tests de integración con base de datos
- **Contract Tests**: Tests de contratos entre servicios
- **Load Tests**: Tests de carga con k6

### Frontend Testing (TypeScript/React)
- **Unit Tests**: Tests de componentes con Vitest
- **Integration Tests**: Tests de flujos completos
- **E2E Tests**: Tests end-to-end con Playwright

## Comandos de Testing

### Backend
```bash
# Ejecutar todos los tests backend
npm run test:backend

# Ejecutar tests con cobertura
npm run test:backend:coverage

# Ejecutar tests de un servicio específico
./gradlew :services:auth-service:test
```

### Frontend
```bash
# Ejecutar tests unitarios
npm run test:unit

# Ejecutar tests en modo watch
npm run test:watch

# Ejecutar tests con cobertura
npm run test:coverage

# Abrir UI de testing
npm run test:ui
```

### E2E
```bash
# Ejecutar tests E2E
npm run test:e2e

# Ejecutar tests E2E en modo debug
npx playwright test --debug
```

### Todos los tests
```bash
# Ejecutar toda la suite de testing
npm run test:all
```

## Cobertura de Código

### Objetivos de Cobertura
- **Backend**: 80% mínimo
- **Frontend**: 70% mínimo

### Reportes
- Backend: `build/reports/jacoco/test/html/index.html`
- Frontend: `coverage/index.html`

## Mejores Prácticas

### Backend Testing
1. Usar `@SpringBootTest` para tests de integración
2. Usar `@WebMvcTest` para tests de controladores
3. Usar Testcontainers para tests con base de datos
4. Mockear dependencias externas

### Frontend Testing
1. Usar Testing Library para tests de componentes
2. Mockear APIs y servicios externos
3. Testear comportamiento, no implementación
4. Usar data-testid para elementos críticos

### E2E Testing
1. Testear flujos críticos de usuario
2. Usar Page Object Model
3. Configurar datos de prueba consistentes
4. Limpiar estado entre tests

## Configuración de CI/CD

Los tests se ejecutan automáticamente en:
- Push a `main` o `develop`
- Pull Requests
- Releases

### Gates de Calidad
- Tests backend deben pasar
- Tests frontend deben pasar
- Cobertura mínima debe cumplirse
- No vulnerabilidades críticas

## Troubleshooting

### Tests Backend Fallan
```bash
# Verificar base de datos de test
docker run --rm -p 5432:5432 -e POSTGRES_PASSWORD=test postgres:15

# Limpiar cache de Gradle
./gradlew clean
```

### Tests Frontend Fallan
```bash
# Limpiar node_modules
rm -rf node_modules package-lock.json
npm install

# Verificar configuración de Vitest
npm run test:ui
```

### Tests E2E Fallan
```bash
# Reinstalar Playwright
npx playwright install --with-deps

# Ejecutar en modo debug
npx playwright test --debug
```
