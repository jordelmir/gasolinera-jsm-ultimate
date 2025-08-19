#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENVIRONMENT=${1:-staging}
PROJECT_NAME="gasolinera-jsm"
BACKUP_DIR="./backups"
LOG_FILE="./deploy-$(date +%Y%m%d-%H%M%S).log"

echo -e "${BLUE}🚀 Starting deployment for ${ENVIRONMENT} environment${NC}"

# Function to log messages
log() {
    echo -e "$1" | tee -a "$LOG_FILE"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
check_prerequisites() {
    log "${YELLOW}📋 Checking prerequisites...${NC}"

    if ! command_exists docker; then
        log "${RED}❌ Docker is not installed${NC}"
        exit 1
    fi

    if ! command_exists docker-compose; then
        log "${RED}❌ Docker Compose is not installed${NC}"
        exit 1
    fi

    if ! command_exists git; then
        log "${RED}❌ Git is not installed${NC}"
        exit 1
    fi

    log "${GREEN}✅ Prerequisites check passed${NC}"
}

# Create backup
create_backup() {
    log "${YELLOW}💾 Creating database backup...${NC}"

    mkdir -p "$BACKUP_DIR"

    if docker ps | grep -q postgres; then
        BACKUP_FILE="$BACKUP_DIR/backup-$(date +%Y%m%d-%H%M%S).sql"
        docker exec postgres pg_dump -U puntog puntog > "$BACKUP_FILE"
        log "${GREEN}✅ Backup created: $BACKUP_FILE${NC}"
    else
        log "${YELLOW}⚠️  PostgreSQL container not running, skipping backup${NC}"
    fi
}

# Build images
build_images() {
    log "${YELLOW}🔨 Building Docker images...${NC}"

    if [ "$ENVIRONMENT" = "production" ]; then
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml build --no-cache
    else
        docker-compose -f docker-compose.yml -f docker-compose.dev.yml build --no-cache
    fi

    log "${GREEN}✅ Images built successfully${NC}"
}

# Deploy services
deploy_services() {
    log "${YELLOW}🚢 Deploying services...${NC}"

    # Load environment variables
    if [ -f ".env.${ENVIRONMENT}" ]; then
        export $(cat .env.${ENVIRONMENT} | grep -v '^#' | xargs)
        log "${GREEN}✅ Environment variables loaded from .env.${ENVIRONMENT}${NC}"
    else
        log "${YELLOW}⚠️  No .env.${ENVIRONMENT} file found, using defaults${NC}"
    fi

    # Deploy based on environment
    if [ "$ENVIRONMENT" = "production" ]; then
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
    else
        docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
    fi

    log "${GREEN}✅ Services deployed successfully${NC}"
}

# Wait for services to be healthy
wait_for_services() {
    log "${YELLOW}⏳ Waiting for services to be healthy...${NC}"

    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
            log "${GREEN}✅ API Gateway is healthy${NC}"
            break
        fi

        log "${YELLOW}⏳ Attempt $attempt/$max_attempts - waiting for API Gateway...${NC}"
        sleep 10
        attempt=$((attempt + 1))
    done

    if [ $attempt -gt $max_attempts ]; then
        log "${RED}❌ Services failed to become healthy${NC}"
        exit 1
    fi
}

# Run database migrations
run_migrations() {
    log "${YELLOW}🗄️  Running database migrations...${NC}"

    # Wait for database to be ready
    sleep 10

    # Run seeding if it's not production
    if [ "$ENVIRONMENT" != "production" ]; then
        make seed-coupon-system || log "${YELLOW}⚠️  Seeding failed or already completed${NC}"
    fi

    log "${GREEN}✅ Database setup completed${NC}"
}

# Verify deployment
verify_deployment() {
    log "${YELLOW}🔍 Verifying deployment...${NC}"

    # Check API Gateway
    if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log "${GREEN}✅ API Gateway is responding${NC}"
    else
        log "${RED}❌ API Gateway is not responding${NC}"
        exit 1
    fi

    # Check Owner Dashboard (if not production)
    if [ "$ENVIRONMENT" != "production" ]; then
        if curl -f http://localhost:3002 >/dev/null 2>&1; then
            log "${GREEN}✅ Owner Dashboard is responding${NC}"
        else
            log "${YELLOW}⚠️  Owner Dashboard is not responding${NC}"
        fi
    fi

    # Check database connection
    if docker exec postgres pg_isready -U puntog >/dev/null 2>&1; then
        log "${GREEN}✅ Database is ready${NC}"
    else
        log "${RED}❌ Database is not ready${NC}"
        exit 1
    fi

    log "${GREEN}✅ Deployment verification passed${NC}"
}

# Cleanup old images
cleanup() {
    log "${YELLOW}🧹 Cleaning up old Docker images...${NC}"

    docker image prune -f
    docker system prune -f --volumes

    log "${GREEN}✅ Cleanup completed${NC}"
}

# Show deployment summary
show_summary() {
    log "${BLUE}📊 Deployment Summary${NC}"
    log "Environment: $ENVIRONMENT"
    log "Timestamp: $(date)"
    log "Log file: $LOG_FILE"
    log ""
    log "${GREEN}🎉 Deployment completed successfully!${NC}"
    log ""
    log "${BLUE}📱 Access URLs:${NC}"

    if [ "$ENVIRONMENT" = "production" ]; then
        log "API Gateway: https://api.gasolinera-jsm.com"
        log "Owner Dashboard: https://admin.gasolinera-jsm.com"
        log "Monitoring: https://monitoring.gasolinera-jsm.com"
    else
        log "API Gateway: http://localhost:8080"
        log "Owner Dashboard: http://localhost:3002"
        log "RabbitMQ Management: http://localhost:15672"
        log "Jaeger Tracing: http://localhost:16686"
    fi

    log ""
    log "${BLUE}🔧 Useful commands:${NC}"
    log "View logs: make logs"
    log "Stop services: make stop"
    log "Clean environment: make clean"
}

# Main deployment flow
main() {
    log "${BLUE}🚀 Gasolinera JSM Deployment Script${NC}"
    log "Environment: $ENVIRONMENT"
    log "Started at: $(date)"
    log ""

    check_prerequisites
    create_backup
    build_images
    deploy_services
    wait_for_services
    run_migrations
    verify_deployment
    cleanup
    show_summary
}

# Handle script interruption
trap 'log "${RED}❌ Deployment interrupted${NC}"; exit 1' INT TERM

# Run main function
main "$@"