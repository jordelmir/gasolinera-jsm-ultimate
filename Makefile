# Makefile for Gasolinera JSM

.PHONY: help dev stop clean test seed mobile k8s-up k8s-down

help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  dev          Starts the development environment using Docker Compose."
	@echo "  stop         Stops the development environment."
	@echo "  clean        Stops and removes all containers, networks, and volumes."
	@echo "  test         Runs all tests for all services."
	@echo "  seed         Seeds the database with initial data."
	@echo "  mobile       Starts the React Native development server."
	@echo "  k8s-up       Deploys the application to a local Kubernetes cluster."
	@echo "  k8s-down     Removes the application from the local Kubernetes cluster."

dev:
	@echo "Starting development environment..."
	docker-compose up --build -d

stop:
	@echo "Stopping development environment..."
	docker-compose down

clean:
	@echo "Cleaning up development environment..."
	docker-compose down -v --remove-orphans

test:
	@echo "Running tests..."
	npm run nx -- run-many --target=test --all

seed:
	@echo "Seeding database..."
	npm run nx -- run ops:seed

mobile:
	@echo "Starting mobile app..."
	cd apps/mobile && npm start

k8s-up:
	@echo "Deploying to Kubernetes..."
	helm upgrade --install gasolinera-jsm ./infra/helm/gasolinera-jsm -f ./infra/helm/gasolinera-jsm/values.yaml

k8s-down:
	@echo "Removing from Kubernetes..."
	helm uninstall gasolinera-jsm
