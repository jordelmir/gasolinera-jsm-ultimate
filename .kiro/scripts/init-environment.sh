#!/bin/bash

# Gasolinera JSM - Environment Initialization Script
# This script sets up the development environment for the project

set -e

echo "🚀 Initializing Gasolinera JSM Development Environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "package.json" ] || [ ! -f "build.gradle.kts" ]; then
    print_error "This script must be run from the project root directory"
    exit 1
fi

# Set environment variables
export NODE_ENV=development
export JAVA_HOME="/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home"
export GRADLE_OPTS="-Xmx4g -Dfile.encoding=UTF-8"
export NX_DAEMON=true
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1
export SPRING_PROFILES_ACTIVE=dev

print_status "Environment variables set"

# Add useful aliases
alias nx='npx nx'
alias gradle='./gradlew'
alias dc='docker-compose'
alias dcd='docker-compose -f docker-compose.dev.yml'
alias dcl='docker-compose logs -f'
alias dcs='docker-compose ps'

print_status "Aliases configured"

# Check required tools
print_status "Checking required tools..."

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    print_success "Node.js found: $NODE_VERSION"
else
    print_error "Node.js not found. Please install Node.js 18+"
fi

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_success "Java found: $JAVA_VERSION"
else
    print_error "Java not found. Please install Java 17+"
fi

# Check Docker
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    print_success "Docker found: $DOCKER_VERSION"
else
    print_error "Docker not found. Please install Docker"
fi

# Check Docker Compose
if command -v docker-compose &> /dev/null; then
    COMPOSE_VERSION=$(docker-compose --version)
    print_success "Docker Compose found: $COMPOSE_VERSION"
else
    print_error "Docker Compose not found. Please install Docker Compose"
fi

# Display available commands
echo ""
print_status "Available development commands:"
echo "  make dev          - Start complete development environment"
echo "  make stop         - Stop all services"
echo "  make logs         - View all service logs"
echo "  make test         - Run all tests"
echo "  make clean        - Clean up containers and volumes"
echo "  make seed         - Seed database with test data"
echo ""
echo "  nx serve admin           - Start admin dashboard (port 3000)"
echo "  nx serve advertiser      - Start advertiser portal (port 3001)"
echo "  nx serve owner-dashboard - Start owner dashboard (port 3002)"
echo ""
echo "  ./gradlew build          - Build all backend services"
echo "  ./gradlew test           - Run backend tests"
echo "  ./gradlew bootRun        - Run individual service"
echo ""

# Display service ports
print_status "Service ports:"
echo "  API Gateway:        http://localhost:8080"
echo "  Auth Service:       http://localhost:8081"
echo "  Redemption Service: http://localhost:8082"
echo "  Station Service:    http://localhost:8083"
echo "  Ad Engine:          http://localhost:8084"
echo "  Raffle Service:     http://localhost:8085"
echo "  Coupon Service:     http://localhost:8086"
echo ""
echo "  Admin Dashboard:    http://localhost:3000"
echo "  Advertiser Portal:  http://localhost:3001"
echo "  Owner Dashboard:    http://localhost:3002"
echo ""
echo "  PostgreSQL:         localhost:5432"
echo "  Redis:              localhost:6379"
echo "  RabbitMQ:           localhost:5672 (Management: 15672)"
echo "  Jaeger UI:          http://localhost:16686"
echo ""

print_success "Environment initialization complete!"
print_status "Run 'make dev' to start the development environment"