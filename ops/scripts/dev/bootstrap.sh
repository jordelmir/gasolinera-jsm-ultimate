#!/bin/bash
set -e

echo "Verifying dependencies..."
docker -v
node -v
npm -v

echo "Copying .env file..."
if [ ! -f .env ]; then
    cp .env.example .env
fi

echo "Starting Docker containers..."
make dev

echo "Bootstrap complete!"
