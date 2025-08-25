#!/bin/bash

# ========================================================
# Script de Validación de Variables de Entorno
# Gasolinera JSM Ultimate
# ========================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Contadores
ERRORS=0
WARNINGS=0
CHECKS=0

echo -e "${BLUE}🔍 Validando configuración de entorno...${NC}"
echo "=================================================="

# Función para verificar variable requerida
check_required() {
    local var_name=$1
    local var_value=${!var_name}
    CHECKS=$((CHECKS + 1))

    if [ -z "$var_value" ]; then
        echo -e "${RED}❌ ERROR: Variable requerida '$var_name' no está definida${NC}"
        ERRORS=$((ERRORS + 1))
        return 1
    else
        echo -e "${GREEN}✅ $var_name${NC}"
        return 0
    fi
}

# Función para verificar variable opcional con warning
check_optional() {
    local var_name=$1
    local var_value=${!var_name}
    CHECKS=$((CHECKS + 1))

    if [ -z "$var_value" ]; then
        echo -e "${YELLOW}⚠️  WARNING: Variable opcional '$var_name' no está definida${NC}"
        WARNINGS=$((WARNINGS + 1))
        return 1
    else
        echo -e "${GREEN}✅ $var_name${NC}"
        return 0
    fi
}

# Función para verificar conectividad
check_connectivity() {
    local service=$1
    local host=$2
    local port=$3
    CHECKS=$((CHECKS + 1))

    echo -n "🔗 Verificando conectividad a $service ($host:$port)... "

    if timeout 5 bash -c "</dev/tcp/$host/$port" 2>/dev/null; then
        echo -e "${GREEN}✅ Conectado${NC}"
        return 0
    else
        echo -e "${RED}❌ No se puede conectar${NC}"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

echo -e "\n${BLUE}📋 Verificando variables básicas...${NC}"
echo "----------------------------------------"

# Variables básicas requeridas
check_required "SPRING_PROFILES_ACTIVE"
check_required "POSTGRES_HOST"
check_required "POSTGRES_PORT"
check_required "POSTGRES_DB"
check_required "POSTGRES_USER"
check_required "POSTGRES_PASSWORD"

echo -e "\n${BLUE}🔐 Verificando secretos de seguridad...${NC}"
echo "----------------------------------------"

# Secretos críticos
check_required "JWT_SECRET"
check_required "QR_PUBLIC_KEY"

# Verificar longitud de JWT_SECRET
if [ -n "$JWT_SECRET" ] && [ ${#JWT_SECRET} -lt 32 ]; then
    echo -e "${YELLOW}⚠️  WARNING: JWT_SECRET debería tener al menos 32 caracteres${NC}"
    WARNINGS=$((WARNINGS + 1))
fi

echo -e "\n${BLUE}🗄️ Verificando servicios de infraestructura...${NC}"
echo "----------------------------------------"

check_required "REDIS_URL"
check_optional "RABBITMQ_HOST"
check_optional "KAFKA_BOOTSTRAP_SERVERS"

echo -e "\n${BLUE}📊 Verificando observabilidad...${NC}"
echo "----------------------------------------"

check_optional "OTEL_EXPORTER_OTLP_ENDPOINT"
check_optional "JAEGER_ENDPOINT"

echo -e "\n${BLUE}⚙️ Verificando configuración específica...${NC}"
echo "----------------------------------------"

check_optional "GEOFENCE_RADIUS_METERS"
check_optional "AD_FALLBACK_URL"
check_optional "JAVA_OPTS"

# Verificar conectividad si estamos en desarrollo
if [ "$SPRING_PROFILES_ACTIVE" = "dev" ] || [ "$SPRING_PROFILES_ACTIVE" = "docker" ]; then
    echo -e "\n${BLUE}🌐 Verificando conectividad (desarrollo)...${NC}"
    echo "----------------------------------------"

    # Solo verificar si las variables están definidas
    if [ -n "$POSTGRES_HOST" ] && [ -n "$POSTGRES_PORT" ]; then
        check_connectivity "PostgreSQL" "$POSTGRES_HOST" "$POSTGRES_PORT"
    fi

    # Extraer host y puerto de REDIS_URL si está definido
    if [ -n "$REDIS_URL" ]; then
        REDIS_HOST=$(echo $REDIS_URL | sed -n 's/redis:\/\/\([^:]*\).*/\1/p')
        if [ -n "$REDIS_HOST" ]; then
            check_connectivity "Redis" "$REDIS_HOST" "6379"
        fi
    fi
fi

# Resumen final
echo -e "\n${BLUE}📊 RESUMEN DE VALIDACIÓN${NC}"
echo "=================================================="
echo -e "Total de verificaciones: $CHECKS"
echo -e "${GREEN}✅ Exitosas: $((CHECKS - ERRORS - WARNINGS))${NC}"
echo -e "${YELLOW}⚠️  Warnings: $WARNINGS${NC}"
echo -e "${RED}❌ Errores: $ERRORS${NC}"

if [ $ERRORS -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ¡Configuración válida! El entorno está listo.${NC}"
    exit 0
else
    echo -e "\n${RED}💥 Configuración inválida. Corrige los errores antes de continuar.${NC}"
    echo -e "\n${BLUE}💡 Sugerencias:${NC}"
    echo "- Verifica que todas las variables requeridas estén en tu archivo .env"
    echo "- Ejecuta: cp ops/env/.env.dev .env (para desarrollo)"
    echo "- Revisa la documentación en ops/env/README.md"
    exit 1
fi