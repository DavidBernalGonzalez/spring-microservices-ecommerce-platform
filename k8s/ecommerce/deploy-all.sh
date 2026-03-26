#!/usr/bin/env bash
# Equivalente Linux/Jenkins de deploy-all.bat: namespace, MySQL, imágenes Docker y microservicios.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BASE_DIR="$SCRIPT_DIR"

KUBECTL="${KUBECTL:-kubectl}"
if ! command -v "${KUBECTL}" >/dev/null 2>&1; then
  echo "Instalando kubectl en /tmp/kubectl..."
  curl -fsSL "https://dl.k8s.io/release/v1.31.2/bin/linux/amd64/kubectl" -o /tmp/kubectl
  chmod +x /tmp/kubectl
  KUBECTL=/tmp/kubectl
fi

echo "=========================================="
echo "Deploy Ecommerce Platform to Kubernetes"
echo "=========================================="
echo

echo "[1/4] Creando namespace y bases de datos..."
"$KUBECTL" apply -f "$BASE_DIR/namespace.yaml"
"$KUBECTL" apply -f "$BASE_DIR/mysql-product.yaml"
"$KUBECTL" apply -f "$BASE_DIR/mysql-order.yaml"
"$KUBECTL" apply -f "$BASE_DIR/mysql-inventory.yaml"

echo
echo "Esperando a que MySQL este listo (60 segundos)..."
sleep 60

echo
echo "[2/4] Construyendo imagenes Docker..."
cd "$PROJECT_ROOT"

docker build -t product-service:latest -f microservices-platform/product-service/Dockerfile microservices-platform/product-service
docker build -t inventory-service:latest -f microservices-platform/inventory-service/Dockerfile microservices-platform/inventory-service
docker build -t order-service:latest -f microservices-platform/order-service/Dockerfile microservices-platform/order-service
docker build -t gateway-service:latest -f microservices-platform/gateway-service/Dockerfile microservices-platform/gateway-service

echo
echo "[3/4] Desplegando microservicios..."
"$KUBECTL" apply -f "$BASE_DIR/product-service.yaml"
"$KUBECTL" apply -f "$BASE_DIR/inventory-service.yaml"
"$KUBECTL" apply -f "$BASE_DIR/order-service.yaml"
"$KUBECTL" apply -f "$BASE_DIR/gateway-service.yaml"

echo
echo "[4/4] Los pods estan arrancando. Puede tardar 2-3 minutos."
echo "Comprueba con: kubectl get pods -n ecommerce"
echo
echo "=========================================="
echo "Despliegue completado"
echo "=========================================="
echo "Gateway: http://localhost:30088 (NodePort)"
echo
