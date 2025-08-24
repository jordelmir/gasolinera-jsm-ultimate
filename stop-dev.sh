#!/bin/bash

echo "🛑 Deteniendo Gasolinera JSM - Entorno de Desarrollo"
echo "=================================================="

# Detener servicios Spring Boot
echo "🔐 Deteniendo Auth Service..."
pkill -f "auth-service" 2>/dev/null || true

echo "🎫 Deteniendo Coupon Service..."
pkill -f "coupon-service" 2>/dev/null || true

echo "🏪 Deteniendo Station Service..."
pkill -f "station-service" 2>/dev/null || true

echo "🔄 Deteniendo Redemption Service..."
pkill -f "redemption-service" 2>/dev/null || true

echo "📺 Deteniendo Ad Engine..."
pkill -f "ad-engine" 2>/dev/null || true

echo "🎰 Deteniendo Raffle Service..."
pkill -f "raffle-service" 2>/dev/null || true

echo "🌐 Deteniendo API Gateway..."
pkill -f "api-gateway" 2>/dev/null || true

# Detener infraestructura Docker
echo "🐘 Deteniendo PostgreSQL y Redis..."
docker-compose -f docker-compose.dev.yml down

echo ""
echo "✅ Todos los servicios han sido detenidos"