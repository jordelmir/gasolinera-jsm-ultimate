#!/bin/bash

# ========================================================
# Script de Análisis de Seguridad
# Gasolinera JSM Ultimate
# ========================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔒 Ejecutando análisis de seguridad completo...${NC}"
echo "=================================================="

# Contadores
CRITICAL_ISSUES=0
HIGH_ISSUES=0
MEDIUM_ISSUES=0
LOW_ISSUES=0

# 1. Análisis de dependencias NPM
echo -e "\n${BLUE}📦 Analizando vulnerabilidades NPM...${NC}"
echo "----------------------------------------"

if command -v npm &> /dev/null; then
    echo -e "${YELLOW}🔍 Ejecutando npm audit...${NC}"

    # Ejecutar npm audit y capturar resultado
    if npm audit --audit-level=moderate > npm-audit.json 2>&1; then
        echo -e "${GREEN}✅ No se encontraron vulnerabilidades críticas en NPM${NC}"
    else
        echo -e "${RED}⚠️  Se encontraron vulnerabilidades en dependencias NPM${NC}"

        # Mostrar resumen
        if command -v jq &> /dev/null; then
            CRITICAL_NPM=$(cat npm-audit.json | jq -r '.metadata.vulnerabilities.critical // 0' 2>/dev/null || echo "0")
            HIGH_NPM=$(cat npm-audit.json | jq -r '.metadata.vulnerabilities.high // 0' 2>/dev/null || echo "0")

            echo -e "   Críticas: ${RED}$CRITICAL_NPM${NC}"
            echo -e "   Altas: ${YELLOW}$HIGH_NPM${NC}"

            CRITICAL_ISSUES=$((CRITICAL_ISSUES + CRITICAL_NPM))
            HIGH_ISSUES=$((HIGH_ISSUES + HIGH_NPM))
        fi

        echo -e "${BLUE}💡 Ejecuta 'npm audit fix' para resolver automáticamente${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  npm no encontrado, saltando análisis NPM${NC}"
fi

# 2. Análisis de dependencias Gradle
echo -e "\n${BLUE}☕ Analizando vulnerabilidades Gradle...${NC}"
echo "----------------------------------------"

if [ -f "./gradlew" ]; then
    echo -e "${YELLOW}🔍 Verificando dependencias Gradle...${NC}"

    # Verificar si hay plugin de seguridad configurado
    if grep -q "org.owasp.dependencycheck" build.gradle.kts; then
        echo -e "${GREEN}✅ OWASP Dependency Check configurado${NC}"
        ./gradlew dependencyCheckAnalyze
    else
        echo -e "${YELLOW}📝 Configurando OWASP Dependency Check...${NC}"

        # Agregar plugin temporalmente para análisis
        echo "
        // Temporary security analysis
        plugins {
            id 'org.owasp.dependencycheck' version '8.4.0'
        }

        dependencyCheck {
            format = 'ALL'
            suppressionFile = 'owasp-suppressions.xml'
            failBuildOnCVSS = 7.0
        }
        " > temp-security.gradle

        echo -e "${BLUE}💡 Considera agregar OWASP Dependency Check permanentemente${NC}"
    fi

    # Verificar dependencias desactualizadas
    echo -e "${YELLOW}🔍 Verificando dependencias desactualizadas...${NC}"
    ./gradlew dependencyUpdates > gradle-updates.txt 2>&1 || true

    if grep -q "outdated" gradle-updates.txt; then
        echo -e "${YELLOW}⚠️  Se encontraron dependencias desactualizadas${NC}"
        MEDIUM_ISSUES=$((MEDIUM_ISSUES + 1))
    else
        echo -e "${GREEN}✅ Dependencias Gradle actualizadas${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  gradlew no encontrado, saltando análisis Gradle${NC}"
fi

# 3. Análisis de secretos en código
echo -e "\n${BLUE}🔐 Buscando secretos hardcodeados...${NC}"
echo "----------------------------------------"

# Patrones de secretos comunes
SECRET_PATTERNS=(
    "password\s*=\s*['\"][^'\"]{8,}['\"]"
    "api[_-]?key\s*=\s*['\"][^'\"]{16,}['\"]"
    "secret\s*=\s*['\"][^'\"]{16,}['\"]"
    "token\s*=\s*['\"][^'\"]{20,}['\"]"
    "-----BEGIN\s+(RSA\s+)?PRIVATE\s+KEY-----"
    "sk_live_[a-zA-Z0-9]{24,}"
    "pk_live_[a-zA-Z0-9]{24,}"
)

SECRETS_FOUND=0

for pattern in "${SECRET_PATTERNS[@]}"; do
    if grep -r -i -E "$pattern" --include="*.kt" --include="*.ts" --include="*.tsx" --include="*.js" --include="*.jsx" --include="*.yaml" --include="*.yml" --include="*.properties" . 2>/dev/null | grep -v node_modules | grep -v .git | grep -v build; then
        SECRETS_FOUND=$((SECRETS_FOUND + 1))
    fi
done

if [ $SECRETS_FOUND -eq 0 ]; then
    echo -e "${GREEN}✅ No se encontraron secretos hardcodeados${NC}"
else
    echo -e "${RED}⚠️  Se encontraron $SECRETS_FOUND posibles secretos hardcodeados${NC}"
    CRITICAL_ISSUES=$((CRITICAL_ISSUES + SECRETS_FOUND))
fi

# 4. Análisis de configuración Docker
echo -e "\n${BLUE}🐳 Analizando configuración Docker...${NC}"
echo "----------------------------------------"

DOCKER_ISSUES=0

# Verificar Dockerfiles
if find . -name "Dockerfile*" -type f | head -1 > /dev/null; then
    echo -e "${YELLOW}🔍 Analizando Dockerfiles...${NC}"

    # Verificar usuario root
    if grep -r "USER root" --include="Dockerfile*" . 2>/dev/null; then
        echo -e "${YELLOW}⚠️  Dockerfiles usando usuario root${NC}"
        DOCKER_ISSUES=$((DOCKER_ISSUES + 1))
    fi

    # Verificar imágenes base sin tag específico
    if grep -r "FROM.*:latest" --include="Dockerfile*" . 2>/dev/null; then
        echo -e "${YELLOW}⚠️  Usando imágenes con tag 'latest'${NC}"
        DOCKER_ISSUES=$((DOCKER_ISSUES + 1))
    fi

    # Verificar secretos en ENV
    if grep -r -i "ENV.*PASSWORD\|ENV.*SECRET\|ENV.*KEY" --include="Dockerfile*" . 2>/dev/null; then
        echo -e "${RED}⚠️  Posibles secretos en variables ENV${NC}"
        DOCKER_ISSUES=$((DOCKER_ISSUES + 2))
    fi

    if [ $DOCKER_ISSUES -eq 0 ]; then
        echo -e "${GREEN}✅ Configuración Docker segura${NC}"
    else
        echo -e "${YELLOW}⚠️  $DOCKER_ISSUES problemas de seguridad en Docker${NC}"
        MEDIUM_ISSUES=$((MEDIUM_ISSUES + DOCKER_ISSUES))
    fi
else
    echo -e "${YELLOW}⚠️  No se encontraron Dockerfiles${NC}"
fi

# 5. Análisis de configuración de seguridad
echo -e "\n${BLUE}⚙️ Verificando configuración de seguridad...${NC}"
echo "----------------------------------------"

SECURITY_CONFIG_ISSUES=0

# Verificar CORS configuration
if grep -r "allowedOrigins.*\*" --include="*.kt" --include="*.yaml" --include="*.yml" . 2>/dev/null; then
    echo -e "${YELLOW}⚠️  CORS configurado para permitir todos los orígenes${NC}"
    SECURITY_CONFIG_ISSUES=$((SECURITY_CONFIG_ISSUES + 1))
fi

# Verificar HTTPS enforcement
if ! grep -r "requiresChannel.*HTTPS\|server.ssl" --include="*.kt" --include="*.yaml" --include="*.yml" . 2>/dev/null; then
    echo -e "${YELLOW}⚠️  HTTPS no parece estar forzado${NC}"
    SECURITY_CONFIG_ISSUES=$((SECURITY_CONFIG_ISSUES + 1))
fi

# Verificar rate limiting
if ! grep -r "RateLimiter\|@RateLimit" --include="*.kt" . 2>/dev/null; then
    echo -e "${YELLOW}⚠️  Rate limiting no configurado${NC}"
    SECURITY_CONFIG_ISSUES=$((SECURITY_CONFIG_ISSUES + 1))
fi

if [ $SECURITY_CONFIG_ISSUES -eq 0 ]; then
    echo -e "${GREEN}✅ Configuración de seguridad básica presente${NC}"
else
    echo -e "${YELLOW}⚠️  $SECURITY_CONFIG_ISSUES problemas de configuración de seguridad${NC}"
    MEDIUM_ISSUES=$((MEDIUM_ISSUES + SECURITY_CONFIG_ISSUES))
fi

# 6. Verificar archivos sensibles
echo -e "\n${BLUE}📁 Verificando archivos sensibles...${NC}"
echo "----------------------------------------"

SENSITIVE_FILES=(
    ".env"
    "*.pem"
    "*.key"
    "*.p12"
    "*.jks"
    "id_rsa"
    "id_dsa"
    "*.sql"
)

SENSITIVE_FOUND=0

for pattern in "${SENSITIVE_FILES[@]}"; do
    if find . -name "$pattern" -not -path "./node_modules/*" -not -path "./.git/*" -not -path "./build/*" 2>/dev/null | head -1 > /dev/null; then
        files=$(find . -name "$pattern" -not -path "./node_modules/*" -not -path "./.git/*" -not -path "./build/*" 2>/dev/null)
        if [ ! -z "$files" ]; then
            echo -e "${YELLOW}⚠️  Archivos sensibles encontrados: $pattern${NC}"
            SENSITIVE_FOUND=$((SENSITIVE_FOUND + 1))
        fi
    fi
done

# Verificar .gitignore
if [ -f ".gitignore" ]; then
    if ! grep -q "\.env" .gitignore; then
        echo -e "${RED}⚠️  .env no está en .gitignore${NC}"
        CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
    fi
    if ! grep -q "\.key\|\.pem" .gitignore; then
        echo -e "${YELLOW}⚠️  Archivos de claves no están en .gitignore${NC}"
        HIGH_ISSUES=$((HIGH_ISSUES + 1))
    fi
fi

if [ $SENSITIVE_FOUND -eq 0 ]; then
    echo -e "${GREEN}✅ No se encontraron archivos sensibles expuestos${NC}"
fi

# 7. Generar reporte final
echo -e "\n${BLUE}📊 REPORTE DE SEGURIDAD${NC}"
echo "=================================================="

# Crear archivo de reporte
cat > security-report.md << EOF
# Security Analysis Report
**Fecha:** $(date)
**Proyecto:** Gasolinera JSM Ultimate

## Resumen de Vulnerabilidades

- **Críticas:** $CRITICAL_ISSUES
- **Altas:** $HIGH_ISSUES
- **Medias:** $MEDIUM_ISSUES
- **Bajas:** $LOW_ISSUES

## Análisis Detallado

### Dependencias NPM
$(if [ -f npm-audit.json ]; then echo "Ver npm-audit.json para detalles"; else echo "No ejecutado"; fi)

### Dependencias Gradle
$(if [ -f gradle-updates.txt ]; then echo "Ver gradle-updates.txt para detalles"; else echo "No ejecutado"; fi)

### Secretos Hardcodeados
$SECRETS_FOUND posibles secretos encontrados

### Configuración Docker
$DOCKER_ISSUES problemas encontrados

### Configuración de Seguridad
$SECURITY_CONFIG_ISSUES problemas encontrados

## Recomendaciones

1. **Inmediatas (Críticas)**
   - Resolver vulnerabilidades críticas en dependencias
   - Eliminar secretos hardcodeados
   - Agregar archivos sensibles a .gitignore

2. **Corto Plazo (Altas)**
   - Actualizar dependencias con vulnerabilidades altas
   - Configurar HTTPS obligatorio
   - Implementar rate limiting

3. **Mediano Plazo (Medias)**
   - Actualizar dependencias desactualizadas
   - Mejorar configuración Docker
   - Configurar CORS restrictivo

## Próximos Pasos

1. Ejecutar: \`npm audit fix\`
2. Revisar y actualizar dependencias Gradle
3. Configurar OWASP Dependency Check
4. Implementar pipeline de seguridad en CI/CD
EOF

echo -e "📄 Reporte guardado en: ${BLUE}security-report.md${NC}"

# Mostrar resumen en consola
echo -e "\n${BLUE}🎯 RESUMEN EJECUTIVO:${NC}"
echo -e "Críticas: ${RED}$CRITICAL_ISSUES${NC}"
echo -e "Altas: ${YELLOW}$HIGH_ISSUES${NC}"
echo -e "Medias: ${BLUE}$MEDIUM_ISSUES${NC}"
echo -e "Bajas: ${GREEN}$LOW_ISSUES${NC}"

# Determinar estado general
TOTAL_ISSUES=$((CRITICAL_ISSUES + HIGH_ISSUES + MEDIUM_ISSUES + LOW_ISSUES))

if [ $CRITICAL_ISSUES -gt 0 ]; then
    echo -e "\n${RED}🚨 ESTADO: CRÍTICO - Resolver inmediatamente${NC}"
    exit 1
elif [ $HIGH_ISSUES -gt 3 ]; then
    echo -e "\n${YELLOW}⚠️  ESTADO: ALTO RIESGO - Resolver pronto${NC}"
    exit 1
elif [ $TOTAL_ISSUES -gt 10 ]; then
    echo -e "\n${YELLOW}⚠️  ESTADO: RIESGO MEDIO - Planificar resolución${NC}"
else
    echo -e "\n${GREEN}✅ ESTADO: ACEPTABLE - Monitorear continuamente${NC}"
fi

echo -e "\n${BLUE}💡 Ejecuta regularmente este script para mantener la seguridad${NC}"