#!/bin/bash

echo "🚀 Iniciando Gasolinera JSM - Modo Simple"
echo "========================================"

# Detener procesos existentes
pkill -f "bootRun" 2>/dev/null || true
sleep 2

# Iniciar infraestructura
echo "🐘 Iniciando PostgreSQL y Redis..."
docker-compose -f docker-compose.dev.yml up -d postgres redis
sleep 8

# Función para iniciar servicio
start_service() {
    local service_name=$1
    local port=$2

    echo "🔄 Iniciando $service_name..."
    cd "services/$service_name"

    # Iniciar en background con logs
    SPRING_PROFILES_ACTIVE=development ../../gradlew bootRun > "/tmp/$service_name.log" 2>&1 &
    local pid=$!
    echo "   PID: $pid"

    cd ../..

    # Esperar un poco
    sleep 5

    # Verificar si está corriendo
    if curl -s "http://localhost:$port/actuator/health" | grep -q "UP"; then
        echo "✅ $service_name funcionando en puerto $port"
    else
        echo "⚠️  $service_name iniciado pero aún no responde"
    fi
}

# Iniciar servicios uno por uno
start_service "auth-service" 8081
start_service "station-service" 8083
start_service "coupon-service" 8086

echo ""
echo "🎉 Servicios iniciados!"
echo "======================"
echo ""
echo "Verificar con: ./check-services.sh"
echo "Ver logs con: tail -f /tmp/auth-service.log"
echo "Detener con: ./stop-dev.sh"