# Ecommerce Platform - Kubernetes

Despliegue completo del ecosistema en Kubernetes (MySQL + microservicios + gateway).

## Requisitos

- Kubernetes (Docker Desktop con K8s habilitado)
- Docker
- kubectl

## Despliegue rapido

```powershell
cd k8s\ecommerce
.\deploy-all.bat
```

## Despliegue manual

### 1. Namespace y MySQL

```bash
kubectl apply -f namespace.yaml
kubectl apply -f mysql-product.yaml
kubectl apply -f mysql-order.yaml
kubectl apply -f mysql-inventory.yaml
```

Esperar 1-2 minutos a que MySQL arranque.

### 2. Construir imagenes

Desde la raiz del proyecto:

```bash
docker build -t product-service:latest -f microservices-platform/product-service/Dockerfile microservices-platform/product-service
docker build -t inventory-service:latest -f microservices-platform/inventory-service/Dockerfile microservices-platform/inventory-service
docker build -t order-service:latest -f microservices-platform/order-service/Dockerfile microservices-platform/order-service
docker build -t gateway-service:latest -f microservices-platform/gateway-service/Dockerfile microservices-platform/gateway-service
```

### 3. Desplegar microservicios

```bash
kubectl apply -f product-service.yaml
kubectl apply -f inventory-service.yaml
kubectl apply -f order-service.yaml
kubectl apply -f gateway-service.yaml
```

## Acceso

- **Gateway:** http://localhost:30088
- **API:** http://localhost:30088/api/v1/products, /orders, /inventory, /categories

## Verificar

```bash
kubectl get pods -n ecommerce
kubectl get svc -n ecommerce
```

## Eliminar todo

```bash
kubectl delete namespace ecommerce
```
