#!/bin/bash

echo "🔍 Diagnóstico Avanzado de Servicios"
echo "=================================="

# Función para diagnosticar servicio
diagnose_service() {
    local service_name=$1
    local port=$2

    echo ""
    echo "🔍 Diagnosticando $service_name (puerto $port):"
    echo "----------------------------------------"

    # Verificar si el puerto está en uso
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "✅ Puerto $port está en uso"

        # Verificar health endpoint
        local health_response=$(curl -s "http://localhost:$port/actuator/health" 2>/dev/null)
        if [ -n "$health_response" ]; then
            echo "✅ Health endpoint responde: $health_response"
        else
            echo "❌ Health endpoint no responde"
        fi

        # Verificar logs recientes
        if [ -f "/tmp/$service_name.log" ]; then
            echo "📝 Últimas líneas del log:"
            tail -5 "/tmp/$service_name.log" | sed 's/^/   /'
        else
            echo "❌ No se encontró archivo de log"
        fi

    else
        echo "❌ Puerto $port no está en uso"

        # Verificar si hay proceso corriendo
        if pgrep -f "$service_name" > /dev/null; then
            echo "⚠️  Proceso existe pero no escucha en el puerto"
        else
            echo "❌ No hay proceso corriendo"
        fi
    fi
}

# Diagnosticar cada servicio
diagnose_service "auth-service" 8081
diagnose_service "station-service" 8083
diagnose_service "coupon-service" 8086
diagnose_service "redemption-service" 8082
diagnose_service "ad-engine" 8084
diagnose_service "raffle-service" 8085

echo ""
echo "🐘 Estado de la infraestructura:"
echo "==============================="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep gasolinera