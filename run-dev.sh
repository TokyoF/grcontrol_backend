#!/bin/bash
# ===================================
# GRControl Backend - Development Run Script
# ===================================

echo ""
echo "[INFO] Loading development environment variables..."
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "[WARNING] .env file not found. Using default values from application.properties"
    echo "[INFO] To customize, copy .env.development to .env"
    echo ""
else
    # Load variables from .env file
    export $(grep -v '^#' .env | xargs)
    echo "[LOADED] Environment variables from .env"
    echo ""
fi

echo "[INFO] Starting GRControl Backend..."
echo "[INFO] Server will be available at http://localhost:${SERVER_PORT:-8080}"
echo ""

./mvnw spring-boot:run
