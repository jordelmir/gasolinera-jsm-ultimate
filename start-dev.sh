#!/bin/bash

echo "🚀 Iniciando Gasolinera JSM - Entorno de Desarrollo"
echo "=================================================="

# Función para verificar si un puerto está en uso
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Puerto en uso
    else
        return 1  # Puerto libre
    fi
}

# Función para esperar que un servicio esté listo
wait_for_service() {
    local url=$1
    local name=$2
    local max_attempts=30
    local attempt=1

    echo "⏳ Esperando que $name esté listo..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo "✅ $name está listo!"
            return 0
        fi
        echo "   Intento $attempt/$max_attempts..."
        sleep 2
        attempt=$((attempt + 1))
    done
    echo "❌ $name no respondió después de $max_attempts intentos"
    return 1
}

# Detener procesos existentes
echo "🛑 Deteniendo servicios existentes..."
pkill -f "bootRun" 2>/dev/null || true
sleep 2

# Iniciar infraestructura
echo "🐘 Iniciando PostgreSQL y Redis..."
docker-compose -f docker-compose.dev.yml up -d postgres redis

# Esperar a que PostgreSQL esté listo
echo "⏳ Esperando a que PostgreSQL esté listo..."
sleep 8

# Verificar conexión a PostgreSQL
if docker exec gasolinera-postgres-dev pg_isready -U gasolinera_dev > /dev/null 2>&1; then
    echo "✅ PostgreSQL está listo!"
else
    echo "❌ PostgreSQL no está respondiendo"
    exit 1
fi

# Iniciar Auth Service
echo "🔐 Iniciando Auth Service..."
if check_port 8081; then
    echo "⚠️  Puerto 8081 ya está en uso, saltando Auth Service"
else
    cd services/auth-service
    SPRING_PROFILES_ACTIVE=development ../../gradlew bootRun --quiet > /tmp/auth-service.log 2>&1 &
    AUTH_PID=$!
    cd ../..

    # Esperar que Auth Service esté listo
    wait_for_service "http://localhost:8081/actuator/health" "Auth Service"
fi

# Iniciar Station Service
echo "🏪 Iniciando Station Service..."
if check_port 8083; then
    echo "⚠️  Puerto 8083 ya está en uso, saltando Station Service"
else
    cd services/station-service
    SPRING_PROFILES_ACTIVE=development ../../gradlew bootRun --quiet > /tmp/station-service.log 2>&1 &
    STATION_PID=$!
    cd ../..

    # Esperar que Station Service esté listo
    wait_for_service "http://localhost:8083/actuator/health" "Station Service"
fi

# Iniciar Coupon Service
echo "🎫 Iniciando Coupon Service..."
if check_port 8086; then
    echo "⚠️  Puerto 8086 ya está en uso, saltando Coupon Service"
else
    cd services/coupon-service
    SPRING_PROFILES_ACTIVE=development ../../gradlew bootRun --quiet > /tmp/coupon-service.log 2>&1 &
    COUPON_PID=$!
    cd ../..

    # Esperar que Coupon Service esté listo
    wait_for_service "http://localhost:8086/actuator/health" "Coupon Service"
fi

echo ""
echo "🎉 ¡GASOLINERA JSM ESTÁ FUNCIONANDO!"
echo "=================================="
echo ""
echo "✅ Servicios activos:"
if check_port 8081; then echo "   🔐 Auth Service:    http://localhost:8081"; fi
if check_port 8083; then echo "   🏪 Station Service: http://localhost:8083"; fi
if check_port 8086; then echo "   🎫 Coupon Service:  http://localhost:8086"; fi
echo ""
echo "📊 Endpoints útiles:"
if check_port 8081; then
    echo "   🔍 Auth Health:     http://localhost:8081/actuator/health"
    echo "   📚 Auth API Docs:   http://localhost:8081/swagger-ui.html"
fi
if check_port 8083; then
    echo "   🔍 Station Health:  http://localhost:8083/actuator/health"
    echo "   📚 Station API Docs: http://localhost:8083/swagger-ui.html"
fi
if check_port 8086; then
    echo "   🔍 Coupon Health:   http://localhost:8086/actuator/health"
    echo "   📚 Coupon API Docs:  http://localhost:8086/swagger-ui.html"
fi
echo ""
echo "📝 Logs disponibles en:"
echo "   /tmp/auth-service.log"
echo "   /tmp/station-service.log"
echo "   /tmp/coupon-service.log"
echo ""
echo "🛑 Para detener: Ctrl+C o ejecutar ./stop-dev.sh"
echo ""

# Mantener el script corriendo
echo "⌨️  Presiona Ctrl+C para detener todos los servicios..."
trap './stop-dev.sh' INT
while true; do
    sleep 1
done