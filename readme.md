# Spring Microservices Ecommerce Platform

Plataforma de e-commerce basada en **microservicios** con Spring Boot, Spring Cloud Gateway y bases de datos MySQL independientes.

---

## Índice

- [Arquitectura](#-arquitectura)
- [Stack tecnológico](#-stack-tecnológico)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Requisitos previos](#-requisitos-previos)
- [Puesta en marcha](#-puesta-en-marcha)
- [API Gateway](#-api-gateway)
- [Endpoints disponibles](#-endpoints-disponibles)
- [Bases de datos](#-bases-de-datos)
- [Contratos y pruebas](#-contratos-y-pruebas)
- [Postman](#-postman)

---

## Arquitectura

```
                    ┌─────────────────────────────────────┐
                    │         API Gateway (8088)           │
                    │    Spring Cloud Gateway (WebFlux)    │
                    └─────────────────┬───────────────────┘
                                      │
         ┌────────────────────────────┼────────────────────────────┐
         │                            │                            │
         ▼                            ▼                            ▼
┌─────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│ product-service │      │   order-service     │      │  inventory-service  │
│     (8081)      │◄─────│      (8082)         │─────►│       (8083)        │
│                 │      │                     │      │                     │
│ • Products      │      │ • Orders            │      │ • Stock             │
│ • Categories    │      │ • OrderItems        │      │ • Reserve/Release   │
└────────┬────────┘      └──────────┬──────────┘      └──────────┬──────────┘
         │                          │                           │
         ▼                          ▼                           ▼
┌─────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│   ms_products   │      │     ms_orders       │      │   ms_inventory      │
│   MySQL :3307   │      │   MySQL :3308       │      │   MySQL :3309       │
└─────────────────┘      └─────────────────────┘      └─────────────────────┘
```

**Flujo de órdenes:**
1. El cliente envía una orden al **gateway** (`/api/v1/orders`).
2. **order-service** valida productos con **product-service** y reserva stock con **inventory-service**.
3. Si todo es correcto, se crea la orden y se confirma la reserva.

---

## Stack tecnológico

| Componente | Tecnología |
|------------|------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4 / 4.0 |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Persistencia | Spring Data JPA |
| Base de datos | MySQL 8 |
| Orquestación | Docker Compose |
| Comunicación | REST (HTTP) |

---

## Estructura del proyecto

```
spring-microservices-ecommerce-platform/
├── microservices-platform/
│   ├── gateway-service/      # API Gateway (puerto 8088)
│   ├── product-service/     # Productos y categorías (puerto 8081)
│   ├── order-service/       # Órdenes (puerto 8082)
│   └── inventory-service/   # Inventario (puerto 8083)
├── contracts/                # Contratos OpenAPI (product-api, inventory-api)
├── postman/                  # Colección Postman para pruebas
├── docker-compose.yml        # Bases de datos MySQL
└── README.md
```

---

## Requisitos previos

- **Java 21**
- **Maven 3.8+**
- **Docker** y **Docker Compose**

Comprobar instalación:

```bash
java -version
mvn -version
docker --version
docker compose version
```

---

## Puesta en marcha

### 1. Levantar las bases de datos

Desde la raíz del proyecto:

```bash
docker compose up -d
```

Se levantan 3 contenedores MySQL en segundo plano. Verificar con:

```bash
docker ps
```

### 2. Iniciar los microservicios

Cada servicio se ejecuta por separado. **Orden recomendado:**

```bash
# Terminal 1 - Product Service
cd microservices-platform/product-service
mvn spring-boot:run

# Terminal 2 - Inventory Service
cd microservices-platform/inventory-service
mvn spring-boot:run

# Terminal 3 - Order Service
cd microservices-platform/order-service
mvn spring-boot:run

# Terminal 4 - Gateway
cd microservices-platform/gateway-service
mvn spring-boot:run
```

### 3. Verificar que todo funciona

- **Gateway:** http://localhost:8088/actuator/health  
- **Productos (vía gateway):** http://localhost:8088/api/v1/products  
- **Categorías (vía gateway):** http://localhost:8088/api/v1/categories  
- **Órdenes (vía gateway):** http://localhost:8088/api/v1/orders  
- **Inventario (vía gateway):** http://localhost:8088/api/v1/inventory  

---

## API Gateway

El **gateway** (puerto **8088**) es el punto de entrada único. Todas las peticiones pasan por él:

| Ruta | Servicio destino | Puerto |
|------|------------------|--------|
| `/api/v1/products/**` | product-service | 8081 |
| `/api/v1/categories/**` | product-service | 8081 |
| `/api/v1/orders/**` | order-service | 8082 |
| `/api/v1/inventory/**` | inventory-service | 8083 |

**Ejemplo:**  
`GET http://localhost:8088/api/v1/products` → se enruta a `http://localhost:8081/api/v1/products`

---

## Endpoints disponibles

### Product Service (`/api/v1/products`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/products` | Listar productos (paginado, filtros: status, categoryId) |
| GET | `/api/v1/products/{id}` | Obtener producto por ID |
| POST | `/api/v1/products` | Crear producto |
| PUT | `/api/v1/products/{id}` | Actualizar producto |
| DELETE | `/api/v1/products/{id}` | Eliminar producto |

### Categories (`/api/v1/categories`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/categories` | Listar categorías |
| GET | `/api/v1/categories/{id}` | Obtener categoría por ID |
| POST | `/api/v1/categories` | Crear categoría |
| PUT | `/api/v1/categories/{id}` | Actualizar categoría |
| DELETE | `/api/v1/categories/{id}` | Eliminar categoría |

### Order Service (`/api/v1/orders`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/orders` | Listar órdenes (paginado) |
| GET | `/api/v1/orders/{id}` | Obtener orden por ID |
| POST | `/api/v1/orders` | Crear orden (con idempotencia) |

### Inventory Service (`/api/v1/inventory`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/inventory` | Listar inventarios (paginado) |
| GET | `/api/v1/inventory/{productId}` | Obtener stock por producto |
| GET | `/api/v1/inventory/{productId}/movements` | Movimientos de stock |
| POST | `/api/v1/inventory` | Crear inventario inicial |
| POST | `/api/v1/inventory/{productId}/add` | Añadir stock |
| POST | `/api/v1/inventory/{productId}/reserve` | Reservar stock |
| POST | `/api/v1/inventory/{productId}/release` | Liberar reserva |
| POST | `/api/v1/inventory/{productId}/confirm-output` | Confirmar salida de stock |

---

## Bases de datos

Cada microservicio tiene su **propia base de datos** (patrón *database per service*):

| Servicio | Base de datos | Puerto | Usuario | Contraseña |
|----------|---------------|--------|---------|------------|
| product-service | `ms_products` | 3307 | davidBernal | admin1234 |
| order-service | `ms_orders` | 3308 | davidBernal | admin1234 |
| inventory-service | `ms_inventory` | 3309 | davidBernal | admin1234 |

**Volúmenes Docker:** `products-data`, `orders-data`, `inventory-data` (persisten los datos al parar contenedores).

**Parar bases de datos:**

```bash
docker compose down
```

---

## Contratos y pruebas

En la carpeta `contracts/` hay contratos OpenAPI que definen las APIs consumidas entre microservicios:

| Archivo | API | Consumidor |
|---------|-----|------------|
| `product-api.yaml` | GET /api/v1/products/{id} | order-service |
| `inventory-api.yaml` | POST reserve, POST release | order-service |

Los tests de contrato en **order-service** validan que el consumidor funciona con respuestas que cumplen el contrato.

---

## Postman

Colección disponible en `postman/Microservices-Ecommerce-Platform.postman_collection.json`.

**Variables por defecto:**
- `product_base_url`: http://localhost:8081  
- `order_base_url`: http://localhost:8082  
- `inventory_base_url`: http://localhost:8083  

Para usar el gateway como punto de entrada, cambia las URLs base a `http://localhost:8088` y mantén las rutas `/api/v1/...`.

---

## Acceso manual a las bases de datos

Herramientas recomendadas: MySQL Workbench, DBeaver, TablePlus.

```
Host: localhost
Puertos: 3307 (products) | 3308 (orders) | 3309 (inventory)
Usuario: davidBernal
Contraseña: admin1234
```
