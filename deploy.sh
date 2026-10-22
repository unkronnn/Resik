#!/bin/bash
set -e

echo "============================================"
echo "  RESIK - Bank Sampah Digital Deployment"
echo "============================================"

REPO_URL="https://github.com/unkronnn/resik.git"
APP_DIR="/opt/resik"

# Install Docker if not present
if ! command -v docker &> /dev/null; then
    echo "[1/6] Installing Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker
    systemctl start docker
else
    echo "[1/6] Docker already installed."
fi

# Install docker-compose if not present
if ! command -v docker compose &> /dev/null; then
    echo "[2/6] Installing Docker Compose plugin..."
    apt-get update -qq && apt-get install -y -qq docker-compose-plugin
else
    echo "[2/6] Docker Compose already installed."
fi

# Clone or pull repo
if [ -d "$APP_DIR" ]; then
    echo "[3/6] Updating existing deployment..."
    cd "$APP_DIR"
    git pull origin main
else
    echo "[3/6] Cloning repository..."
    git clone "$REPO_URL" "$APP_DIR"
    cd "$APP_DIR"
fi

# Build and start
echo "[4/6] Building and starting containers..."
docker compose down 2>/dev/null || true
docker compose build --no-cache
docker compose up -d

# Wait for health
echo "[5/6] Waiting for API to start..."
sleep 15

# Health check
echo "[6/6] Health check..."
if curl -s http://localhost:8081/api/sampah | grep -q "id"; then
    echo ""
    echo "============================================"
    echo "  DEPLOYMENT BERHASIL!"
    echo "============================================"
    echo "  API: http://43.157.243.87:8081/api"
    echo "  Sampah endpoint: http://43.157.243.87:8081/api/sampah"
    echo "  PostgreSQL: localhost:5433 (db: resik, user: resik)"
    echo "============================================"
else
    echo "  Waiting for API..."
    sleep 15
    if curl -s http://localhost:8081/api/sampah | grep -q "id"; then
        echo "  API is now running!"
    else
        echo "  WARNING: API might still be starting. Check: docker compose logs"
    fi
fi
