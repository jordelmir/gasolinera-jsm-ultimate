# Makefile for Gasolinera JSM

.PHONY: help build-all dev dev-frontend stop clean logs test seed mobile k8s-up k8s-down lint format check-deps

help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Development:"
	@echo "  dev          Starts the full development environment using Docker Compose."
	@echo "  dev-frontend Starts only frontend apps in development mode."
	@echo "  stop         Stops the development environment."
	@echo "  clean        Stops and removes all containers, networks, and volumes."
	@echo "  logs         Follows the logs of all running services."
	@echo ""
	@echo "Build & Test:"
	@echo "  build-all    Builds all Docker images for the services."
	@echo "  test         Runs all tests for all services."
	@echo "  lint         Runs linting for all projects."
	@echo "  format       Formats code using Prettier."
	@echo ""
	@echo "Data & Mobile:"
	@echo "  seed         Seeds the database with initial data."
	@echo "  mobile       Starts the React Native development server."
	@echo ""
	@echo "Kubernetes:"
	@echo "  k8s-up       Deploys the application to a local Kubernetes cluster."
	@echo "  k8s-down     Removes the application from the local Kubernetes cluster."
	@echo ""
	@echo "Utilities:"
	@echo "  check-deps   Checks for outdated dependencies."

build-all:
	@echo "Building all Docker images..."
	docker compose build

dev:
	@echo "Starting development environment..."
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d

dev-frontend:
	@echo "Starting frontend development servers..."
	npm run nx -- run-many --target=serve --projects=admin,advertiser --parallel

stop:
	@echo "Stopping development environment..."
	docker compose down

clean:
	@echo "Cleaning up development environment..."
	docker compose down -v --remove-orphans

logs:
	@echo "Following logs..."
	docker compose logs -f

test:
	@echo "Running tests..."
	npm run nx -- run-many --target=test --all

seed:
	@echo "Seeding database..."
	npm run nx -- run ops:seed

seed-coupon-system:
	@echo "Seeding coupon system data..."
	ts-node ops/scripts/seed-coupon-system.ts

mobile:
	@echo "Starting mobile app..."
	cd apps/mobile && npm start

k8s-up:
	@echo "Deploying to Kubernetes..."
	helm upgrade --install gasolinera-jsm ./infra/helm/gasolinera-jsm -f ./infra/helm/gasolinera-jsm/values.yaml

k8s-down:
	@echo "Removing from Kubernetes..."
	helm uninstall gasolinera-jsm
lint:
	@echo "Running linting..."
	npm run lint

format:
	@echo "Formatting code..."
	npm run format

check-deps:
	@echo "Checking for outdated dependencies..."
	npm outdated

# Environment validation
ops\:env\:validate:
	@echo "Validating environment configuration..."
	./ops/scripts/validate-env.sh

# Mobile development
client-mobile:
	@echo "Starting client mobile app..."
	cd apps/client-mobile && npm start

employee-mobile:
	@echo "Starting employee mobile app..."
	cd apps/employee-mobile && npm start

# Frontend development
owner-dashboard:
	@echo "Starting owner dashboard..."
	cd apps/owner-dashboard && npm run dev

# Full system commands
dev-mobile:
	@echo "Starting all mobile apps..."
	make client-mobile & make employee-mobile

dev-web:
	@echo "Starting all web apps..."
	make owner-dashboard & npm run nx -- serve admin --port 3000 & npm run nx -- serve advertiser --port 3001

# Production deployment
deploy-staging:
	@echo "Deploying to staging..."
	docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d

deploy-production:
	@echo "Deploying to production..."
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Database operations
db-migrate:
	@echo "Running database migrations..."
	npm run nx -- run ops:migrate

db-backup:
	@echo "Creating database backup..."
	docker exec postgres pg_dump -U puntog puntog > backup_$(shell date +%Y%m%d_%H%M%S).sql

db-restore:
	@echo "Restoring database from backup..."
	@read -p "Enter backup file path: " backup_file; \
	docker exec -i postgres psql -U puntog -d puntog < $$backup_file