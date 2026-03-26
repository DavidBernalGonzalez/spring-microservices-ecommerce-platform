#!/usr/bin/env bash
# Mismo flujo que dev-local-test-and-deploy.bat: CI + deploy-all en K8s local.
# Uso (raíz del repo): bash scripts/dev-local-test-and-deploy.sh
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "========== 1/2 Tests (ci-local) =========="
bash scripts/ci-local.sh

echo ""
echo "========== 2/2 Deploy local a Kubernetes =========="
echo "Contexto kubectl:"
kubectl config current-context || true
echo ""

chmod +x "$ROOT/k8s/ecommerce/deploy-all.sh"
bash "$ROOT/k8s/ecommerce/deploy-all.sh"

echo ""
echo "Listo: tests OK y deploy lanzado contra tu cluster local."
