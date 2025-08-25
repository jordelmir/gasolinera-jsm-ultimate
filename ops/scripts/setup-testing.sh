#!/bin/bash

# ========================================================
# Script de Configuración de Testing
# Gasolinera JSM Ultimate
# ========================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧪 Configurando sistema de testing completo...${NC}"
echo "=================================================="

# 1. Backend Testing (Kotlin/Spring Boot)
echo -e "\n${BLUE}🔧 Configurando testing backend...${NC}"

# Agregar JaCoCo para cobertura de código en build.gradle.kts principal
if ! grep -q "jacoco" build.gradle.kts; then
    echo -e "${YELLOW}📝 Agregando JaCoCo plugin...${NC}"

    # Crear backup
    cp build.gradle.kts build.gradle.kts.backup

    # Agregar plugin JaCoCo
    sed -i '' '/id("io.gitlab.arturbosch.detekt")/a\
    id("jacoco") apply false
' build.gradle.kts

    echo -e "${GREEN}✅ JaCoCo plugin agregado${NC}"
fi

# 2. Frontend Testing (Jest/Vitest)
echo -e "\n${BLUE}🔧 Configurando testing frontend...${NC}"

# Verificar si Vitest está configurado
if ! grep -q "vitest" package.json; then
    echo -e "${YELLOW}📦 Instalando Vitest y dependencias de testing...${NC}"

    npm install --save-dev vitest @vitest/ui @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom

    echo -e "${GREEN}✅ Dependencias de testing instaladas${NC}"
fi

# 3. E2E Testing (Playwright)
echo -e "\n${BLUE}🔧 Verificando Playwright...${NC}"

if [ -d "apps/admin-e2e" ]; then
    echo -e "${GREEN}✅ Playwright ya configurado en admin-e2e${NC}"
else
    echo -e "${YELLOW}📝 Configurando Playwright...${NC}"
    npx create-nx-workspace@latest --preset=empty --name=e2e-tests --packageManager=npm
fi

# 4. Crear configuración de cobertura
echo -e "\n${BLUE}📊 Configurando reportes de cobertura...${NC}"

# Crear configuración de Vitest
cat > vitest.config.ts << 'EOF'
import { defineConfig } from 'vitest/config'
import { resolve } from 'path'

export default defineConfig({
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./test-setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'dist/',
        '.nx/',
        'coverage/',
        '**/*.d.ts',
        '**/*.config.{js,ts}',
        '**/test-setup.ts'
      ],
      thresholds: {
        global: {
          branches: 70,
          functions: 70,
          lines: 70,
          statements: 70
        }
      }
    }
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, './apps'),
      '@shared': resolve(__dirname, './packages/shared')
    }
  }
})
EOF

# Crear setup de testing
cat > test-setup.ts << 'EOF'
import '@testing-library/jest-dom'

// Mock para Next.js router
jest.mock('next/router', () => ({
  useRouter() {
    return {
      route: '/',
      pathname: '/',
      query: {},
      asPath: '/',
      push: jest.fn(),
      pop: jest.fn(),
      reload: jest.fn(),
      back: jest.fn(),
      prefetch: jest.fn().mockResolvedValue(undefined),
      beforePopState: jest.fn(),
      events: {
        on: jest.fn(),
        off: jest.fn(),
        emit: jest.fn(),
      },
    }
  },
}))

// Mock para variables de entorno
process.env.NEXT_PUBLIC_API_URL = 'http://localhost:8080'
EOF

echo -e "${GREEN}✅ Configuración de cobertura creada${NC}"

# 5. Crear tests de ejemplo
echo -e "\n${BLUE}🧪 Creando tests de ejemplo...${NC}"

# Test de ejemplo para backend
mkdir -p services/api-gateway/src/test/kotlin/com/gasolinerajsm/gateway
cat > services/api-gateway/src/test/kotlin/com/gasolinerajsm/gateway/GatewayApplicationTest.kt << 'EOF'
package com.gasolinerajsm.gateway

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class GatewayApplicationTest {

    @Test
    fun contextLoads() {
        // Test que verifica que el contexto de Spring se carga correctamente
    }
}
EOF

# Test de ejemplo para frontend
mkdir -p apps/admin/src/components/__tests__
cat > apps/admin/src/components/__tests__/example.test.tsx << 'EOF'
import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'

// Componente de ejemplo para testing
const ExampleComponent = () => {
  return <div>Hello Testing World</div>
}

describe('ExampleComponent', () => {
  it('renders correctly', () => {
    render(<ExampleComponent />)
    expect(screen.getByText('Hello Testing World')).toBeInTheDocument()
  })
})
EOF

echo -e "${GREEN}✅ Tests de ejemplo creados${NC}"

# 6. Configurar scripts de testing en package.json
echo -e "\n${BLUE}📝 Actualizando scripts de testing...${NC}"

# Backup del package.json
cp package.json package.json.backup

# Usar Node.js para actualizar package.json
node -e "
const fs = require('fs');
const pkg = JSON.parse(fs.readFileSync('package.json', 'utf8'));

pkg.scripts = {
  ...pkg.scripts,
  'test:unit': 'vitest run',
  'test:watch': 'vitest',
  'test:coverage': 'vitest run --coverage',
  'test:ui': 'vitest --ui',
  'test:backend': './gradlew test',
  'test:backend:coverage': './gradlew test jacocoTestReport',
  'test:e2e': 'nx run-many --target=e2e --all',
  'test:all': 'npm run test:backend && npm run test:unit && npm run test:e2e'
};

fs.writeFileSync('package.json', JSON.stringify(pkg, null, 2));
"

echo -e "${GREEN}✅ Scripts de testing actualizados${NC}"

# 7. Configurar GitHub Actions para testing
echo -e "\n${BLUE}🔄 Configurando CI para testing...${NC}"

mkdir -p .github/workflows

cat > .github/workflows/test.yml << 'EOF'
name: Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  backend-tests:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: test_password
          POSTGRES_USER: test_user
          POSTGRES_DB: test_db
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432

      redis:
        image: redis:7
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 6379:6379

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-

    - name: Run backend tests
      run: ./gradlew test
      env:
        POSTGRES_HOST: localhost
        POSTGRES_PORT: 5432
        POSTGRES_DB: test_db
        POSTGRES_USER: test_user
        POSTGRES_PASSWORD: test_password
        REDIS_URL: redis://localhost:6379

    - name: Generate test report
      run: ./gradlew jacocoTestReport
      if: always()

    - name: Upload coverage reports
      uses: codecov/codecov-action@v4
      if: always()
      with:
        file: ./build/reports/jacoco/test/jacocoTestReport.xml

  frontend-tests:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: '18'
        cache: 'npm'

    - name: Install dependencies
      run: npm ci

    - name: Run frontend tests
      run: npm run test:coverage

    - name: Upload coverage reports
      uses: codecov/codecov-action@v4
      if: always()
      with:
        file: ./coverage/coverage-final.json

  e2e-tests:
    runs-on: ubuntu-latest
    needs: [backend-tests, frontend-tests]

    steps:
    - uses: actions/checkout@v4

    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: '18'
        cache: 'npm'

    - name: Install dependencies
      run: npm ci

    - name: Install Playwright
      run: npx playwright install --with-deps

    - name: Run E2E tests
      run: npm run test:e2e

    - name: Upload test results
      uses: actions/upload-artifact@v4
      if: always()
      with:
        name: playwright-report
        path: playwright-report/
        retention-days: 30
EOF

echo -e "${GREEN}✅ GitHub Actions configurado${NC}"

# 8. Crear documentación de testing
echo -e "\n${BLUE}📚 Creando documentación de testing...${NC}"

cat > docs/TESTING.md << 'EOF'
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
EOF

echo -e "${GREEN}✅ Documentación de testing creada${NC}"

# Resumen final
echo -e "\n${BLUE}📊 RESUMEN DE CONFIGURACIÓN${NC}"
echo "=================================================="
echo -e "${GREEN}✅ JaCoCo configurado para cobertura backend${NC}"
echo -e "${GREEN}✅ Vitest configurado para testing frontend${NC}"
echo -e "${GREEN}✅ Playwright verificado para E2E${NC}"
echo -e "${GREEN}✅ Scripts de testing agregados${NC}"
echo -e "${GREEN}✅ GitHub Actions configurado${NC}"
echo -e "${GREEN}✅ Documentación creada${NC}"

echo -e "\n${BLUE}🚀 PRÓXIMOS PASOS:${NC}"
echo "1. Ejecutar: npm run test:all"
echo "2. Revisar cobertura: npm run test:coverage"
echo "3. Escribir tests específicos para tu lógica de negocio"
echo "4. Configurar tests de carga con k6"

echo -e "\n${GREEN}🎉 ¡Sistema de testing configurado exitosamente!${NC}"