#!/usr/bin/env bash
# Mismo CI que Jenkins (stage "CI - Maven"). Ejecutar desde la raíz del repo.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
for svc in product-service inventory-service order-service gateway-service; do
  echo "=== ${svc} ==="
  (cd "${ROOT}/microservices-platform/${svc}" && chmod +x mvnw && ./mvnw -B clean install)
done
