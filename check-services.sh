#!/bin/bash

echo "🔍 Verificando Estado de Servicios Gasolinera JSM"
echo "=============================================="

# Función para verificar servicio
check_service() {
    local port=$1
    local name=$2
    local url="http://localhost:$port/actuator/health"

    if curl -s "$url" | grep -q '"status":"UP"'; then
        echo "✅ $name (puerto $port): FUNCIONANDO"
        return 0
    else
        echo "❌ $name (puerto $port): NO DISPONIBLE"
        return 1
    fi
}

# Verificar infraestructura
echo ""
echo "🐘 Infraestructura:"
if docker ps | grep -q "gasolinera-postgres-dev"; then
    echo "✅ PostgreSQL: FUNCIONANDO"
else
    echo "❌ PostgreSQL: NO DISPONIBLE"
fi

if docker ps | grep -q "gasolinera-redis-dev"; then
    echo "✅ Redis: FUNCIONANDO"
else
    echo "❌ Redis: NO DISPONIBLE"
fi

# Verificar servicios
echo ""
echo "🚀 Microservicios:"
check_service 8081 "Auth Service"
check_service 8083 "Station Service"
check_service 8086 "Coupon Service"
check_service 8082 "Redemption Service"
check_service 8084 "Ad Engine"
check_service 8085 "Raffle Service"
check_service 8080 "API Gateway"

echo ""
echo "📊 Resumen:"
active_services=$(curl -s http://localhost:8081/actuator/health http://localhost:8083/actuator/health http://localhost:8086/actuator/health 2>/dev/null | grep -c '"status":"UP"')
echo "   Servicios activos: $active_services/7"

if [ $active_services -gt 0 ]; then
    echo ""
    echo "🎉 ¡La aplicación está funcionando!"
    echo "   Prueba: curl http://localhost:8081/actuator/health"
else
    echo ""
    echo "⚠️  Ningún servicio está funcionando"
    echo "   Ejecuta: ./start-dev.sh"
fi