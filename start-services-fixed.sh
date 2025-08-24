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
    local max_attempts=20
    local attempt=1

    echo "⏳ Esperando que $name esté listo..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" | grep -q "UP" 2>/dev/null; then
            echo "✅ $name está listo!"
            return 0
        fi
        printf "   Intento %d/%d...\n" $attempt $max_attempts
        sleep 3
        attempt=$((attempt + 1))
    done
    echo "⚠️  $name no respondió después de $max_attempts intentos"
    return 1
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

    cd "$service_dir" || { echo "❌ No se pudo acceder a $service_dir"; return 1; }
    SPRING_PROFILES_ACTIVE=development ../../gradlew bootRun --quiet > "/tmp/${service_name,,}.log" 2>&1 &
    local pid=$!
    cd ../.. || return 1

    echo "   PID: $pid, Log: /tmp/${service_name,,}.log"
    return 0
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

echo ""
echo "🚀 Iniciando microservicios..."
echo "============================="

# Iniciar servicios uno por uno con espera
start_service "auth-service" 8081 "services/auth-service"
sleep 5
wait_for_service "http://localhost:8081/actuator/health" "Auth Service"

start_service "station-service" 8083 "services/station-service"
sleep 5
wait_for_service "http://localhost:8083/actuator/health" "Station Service"

start_service "coupon-service" 8086 "services/coupon-service"
sleep 5
wait_for_service "http://localhost:8086/actuator/health" "Coupon Service"

start_service "redemption-service" 8082 "services/redemption-service"
sleep 5
wait_for_service "http://localhost:8082/actuator/health" "Redemption Service"

start_service "ad-engine" 8084 "services/ad-engine"
sleep 5
wait_for_service "http://localhost:8084/actuator/health" "Ad Engine"

start_service "raffle-service" 8085 "services/raffle-service"
sleep 5
wait_for_service "http://localhost:8085/actuator/health" "Raffle Service"

echo ""
echo "🎉 ¡GASOLINERA JSM FUNCIONANDO!"
echo "==============================="
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
if check_port 8081; then echo "   curl http://localhost:8081/actuator/health"; fi
if check_port 8083; then echo "   curl http://localhost:8083/actuator/health"; fi
if check_port 8086; then echo "   curl http://localhost:8086/actuator/health"; fi
if check_port 8082; then echo "   curl http://localhost:8082/actuator/health"; fi
if check_port 8084; then echo "   curl http://localhost:8084/actuator/health"; fi
if check_port 8085; then echo "   curl http://localhost:8085/actuator/health"; fi

echo ""
echo "📝 Ver logs:"
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
echo "✨ ¡Todos los servicios iniciados exitosamente!"