#!/bin/bash

echo "🚀 Iniciando TODOS los Servicios de Gasolinera JSM"
echo "================================================="

# Función para verificar si un puerto está en uso
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Puerto en uso
    else
        return 1  # Puerto libre
    fi
}

# Función para iniciar un servicio
start_service() {
    local service_name=$1
    local port=$2
    local service_dir=$3

    echo "🔄 Iniciando $service_name..."
    if check_port $port; then
        echo "⚠️  Puerto $port ya está en uso, saltando $service_name"
        return 0
    fi

    cd $service_dir || return 1
    SPRING_PROFILES_ACTIVE=development ../../gradlew bootRun --quiet > /tmp/${service_name,,}.log 2>&1 &
    local pid=$!
    cd ../..

    echo "   PID: $pid, Log: /tmp/${service_name,,}.log"
    return 0
}

# Función para esperar que un servicio esté listo
wait_for_service() {
    local url=$1
    local name=$2
    local max_attempts=15
    local attempt=1

    echo "⏳ Esperando que $name responda..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo "✅ $name está listo!"
            return 0
        fi
        echo "   Intento $attempt/$max_attempts..."
        sleep 3
        attempt=$((attempt + 1))
    done
    echo "⚠️  $name no respondió, pero continuando..."
    return 1
}

# Detener procesos existentes
echo "🛑 Deteniendo servicios existentes..."
pkill -f "bootRun" 2>/dev/null || true
sleep 3

# Iniciar infraestructura
echo "🐘 Iniciando PostgreSQL y Redis..."
docker-compose -f docker-compose.dev.yml up -d postgres redis

# Esperar a que PostgreSQL esté listo
echo "⏳ Esperando a que PostgreSQL esté listo..."
sleep 10

# Verificar conexión a PostgreSQL
if docker exec gasolinera-postgres-dev pg_isready -U gasolinera_dev > /dev/null 2>&1; then
    echo "✅ PostgreSQL está listo!"
else
    echo "❌ PostgreSQL no está respondiendo"
    exit 1
fi

echo ""
echo "🚀 Iniciando todos los microservicios..."
echo "======================================"

# Iniciar servicios en paralelo
start_service "Auth-Service" 8081 "services/auth-service"
start_service "Station-Service" 8083 "services/station-service"
start_service "Coupon-Service" 8086 "services/coupon-service"
start_service "Redemption-Service" 8082 "services/redemption-service"
start_service "Ad-Engine" 8084 "services/ad-engine"
start_service "Raffle-Service" 8085 "services/raffle-service"

echo ""
echo "⏳ Esperando que los servicios estén listos..."
echo "============================================="

# Esperar que los servicios estén listos
sleep 15

# Verificar servicios
echo ""
echo "🔍 Verificando servicios..."
wait_for_service "http://localhost:8081/actuator/health" "Auth Service"
wait_for_service "http://localhost:8083/actuator/health" "Station Service"
wait_for_service "http://localhost:8086/actuator/health" "Coupon Service"
wait_for_service "http://localhost:8082/actuator/health" "Redemption Service"
wait_for_service "http://localhost:8084/actuator/health" "Ad Engine"
wait_for_service "http://localhost:8085/actuator/health" "Raffle Service"

echo ""
echo "🎉 ¡TODOS LOS SERVICIOS INICIADOS!"
echo "================================="
echo ""
echo "✅ Servicios disponibles:"
if check_port 8081; then echo "   🔐 Auth Service:       http://localhost:8081"; fi
if check_port 8083; then echo "   🏪 Station Service:    http://localhost:8083"; fi
if check_port 8086; then echo "   🎫 Coupon Service:     http://localhost:8086"; fi
if check_port 8082; then echo "   🔄 Redemption Service: http://localhost:8082"; fi
if check_port 8084; then echo "   📺 Ad Engine:          http://localhost:8084"; fi
if check_port 8085; then echo "   🎰 Raffle Service:     http://localhost:8085"; fi

echo ""
echo "📊 Health Checks:"
if check_port 8081; then echo "   🔍 Auth:       http://localhost:8081/actuator/health"; fi
if check_port 8083; then echo "   🔍 Station:    http://localhost:8083/actuator/health"; fi
if check_port 8086; then echo "   🔍 Coupon:     http://localhost:8086/actuator/health"; fi
if check_port 8082; then echo "   🔍 Redemption: http://localhost:8082/actuator/health"; fi
if check_port 8084; then echo "   🔍 Ad Engine:  http://localhost:8084/actuator/health"; fi
if check_port 8085; then echo "   🔍 Raffle:     http://localhost:8085/actuator/health"; fi

echo ""
echo "📚 API Documentation:"
if check_port 8081; then echo "   📖 Auth:       http://localhost:8081/swagger-ui.html"; fi
if check_port 8083; then echo "   📖 Station:    http://localhost:8083/swagger-ui.html"; fi
if check_port 8086; then echo "   📖 Coupon:     http://localhost:8086/swagger-ui.html"; fi

echo ""
echo "📝 Logs disponibles:"
echo "   tail -f /tmp/auth-service.log"
echo "   tail -f /tmp/station-service.log"
echo "   tail -f /tmp/coupon-service.log"
echo "   tail -f /tmp/redemption-service.log"
echo "   tail -f /tmp/ad-engine.log"
echo "   tail -f /tmp/raffle-service.log"

echo ""
echo "🛑 Para detener: ./stop-dev.sh"
echo "🔍 Para verificar: ./check-services.sh"

echo ""
echo "⌨️  Presiona Ctrl+C para salir (servicios seguirán corriendo)..."
trap 'echo "👋 Saliendo... Los servicios siguen corriendo. Usa ./stop-dev.sh para detenerlos."' INT
while true; do
    sleep 1
done