# Spring Microservices Ecommerce Platform

Plataforma de e-commerce basada en **microservicios** con Spring Boot, Spring Cloud Gateway y bases de datos MySQL independientes.

---

## Índice

- [Arquitectura](#-arquitectura)
- [Stack tecnológico](#-stack-tecnológico)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Infraestructura](#-infraestructura)
- [Requisitos previos](#-requisitos-previos)
- [Scripts de arranque (.bat)](#-scripts-de-arranque-bat)
- [Puesta en marcha](#-puesta-en-marcha)
- [API Gateway](#-api-gateway)
- [Endpoints disponibles](#-endpoints-disponibles)
- [Bases de datos](#-bases-de-datos)
- [CI/CD](#-cicd)
- [Contratos y pruebas](#-contratos-y-pruebas)
- [Postman](#-postman)

---

## Arquitectura

```
                          ┌─────────────────────────────────────┐
                          │         API Gateway (8088)          │
                          │    Spring Cloud Gateway (WebFlux)   │
                          │         (enruta peticiones)         │
                          └─────────────────┬───────────────────┘
                                            │
              ┌─────────────────────────────┼─────────────────────────────┐
              │                             │                             │
              ▼                             ▼                             ▼
    ┌──────────────────┐          ┌──────────────────┐          ┌───────────────────┐
    │  product-service │◄─────────│  order-service   │─────────►│ inventory-service │
    │      (8081)      │  Feign   │      (8082)      │  Feign   │      (8083)       │
    │                  │  Client  │                  │  Client  │                   │
    │ • Products       │          │ • Orders         │          │ • Stock           │
    │ • Categories     │          │ • OrderItems     │          │ • Reserve/Release │
    └────────┬─────────┘          └────────┬─────────┘          └────────┬──────────┘
             ▲                             │                             │
             │ RestClient                  │                             │
             └─────────────────────────────┼─────────────────────────────┘
             │         inventory → product (valida existe)               │
             │                                                           │
             ▼                                                           ▼
    ┌──────────────────┐         ┌──────────────────┐          ┌──────────────────┐
    │   ms_products    │         │    ms_orders     │          │   ms_inventory   │
    │   MySQL :3307    │         │   MySQL :3308    │          │   MySQL :3309    │
    └──────────────────┘         └──────────────────┘          └──────────────────┘

    Llamadas entre servicios:
    • order-service ──FeignClient──► product-service  (ProductClient: GET /products/{id})
    • order-service ──FeignClient──► inventory-service (InventoryClient: reserve, release)
    • inventory-service ──RestClient──► product-service (ProductClient: valida producto existe)
```

**Flujo de órdenes:**
1. El cliente envía una orden al **gateway** (`/api/v1/orders`).
2. **order-service** valida productos con **product-service** y reserva stock con **inventory-service**.
3. Si todo es correcto, se crea la orden y se confirma la reserva.

**Comunicación entre microservicios (clientes HTTP):**

| Microservicio | Consume | Cliente usado | Descripción |
|---------------|---------|---------------|-------------|
| **order-service** | product-service | **FeignClient** | `ProductClient` – obtiene producto por ID para validar items de la orden |
| **order-service** | inventory-service | **FeignClient** | `InventoryClient` – reserva y libera stock |
| **inventory-service** | product-service | **RestClient** | `ProductClient` – valida que el producto exista antes de crear inventario |
| **product-service** | — | — | No consume otros servicios |
| **gateway-service** | — | — | Solo enruta; no hace llamadas HTTP a los microservicios |

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
| Clientes HTTP | **FeignClient** (order-service), **RestClient** (inventory-service) |

---

## Estructura del proyecto

```
spring-microservices-ecommerce-platform/
├── microservices-platform/
│   ├── gateway-service/      # API Gateway (puerto 8088) – sin clientes HTTP
│   ├── product-service/      # Productos y categorías (puerto 8081) – sin clientes HTTP
│   ├── order-service/        # Órdenes (puerto 8082) – FeignClient → product, inventory
│   └── inventory-service/    # Inventario (puerto 8083) – RestClient → product
├── k8s/                      # Manifiestos Kubernetes
│   ├── jenkins/              # Jenkins (CI) – Helm + JCasC
│   └── ecommerce/            # Despliegue app en K8s
├── contracts/                # Contratos OpenAPI (product-api, inventory-api)
├── postman/                  # Colecciones Postman (DEV y PROD)
├── docs/                     # Documentación adicional (Jenkins, etc.)
├── docker-compose.yml        # Bases de datos MySQL (entorno local)
├── start-dev.bat             # Arranca entorno local (DB + microservicios)
├── stop-db.bat               # Para bases de datos Docker
└── README.md
```

---

## Infraestructura

Esta sección describe la infraestructura que hemos montado para CI/CD y despliegue: Kubernetes, Jenkins, Docker y la red entre servicios.

### Visión general

La infraestructura se divide en dos entornos dentro del mismo clúster Kubernetes:

| Namespace | Contenido | Propósito |
|-----------|------------|-----------|
| `jenkins` | Jenkins controller + agentes | CI: compilar y probar en cada push |
| `ecommerce` | MySQL (3) + 4 microservicios | CD: aplicación desplegada |

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        CLÚSTER KUBERNETES                                │
├─────────────────────────────────┬───────────────────────────────────────┤
│  Namespace: jenkins             │  Namespace: ecommerce                  │
│                                 │                                         │
│  ┌─────────────────────────┐   │   ┌─────────────┐  ┌─────────────┐    │
│  │ Jenkins (Helm)          │   │   │ mysql-      │  │ mysql-      │    │
│  │ - Controller (StatefulSet)   │   │ product     │  │ order       │    │
│  │ - NodePort 30080         │   │   │ ClusterIP   │  │ ClusterIP   │    │
│  │ - PVC 8Gi                │   │   └──────┬──────┘  └──────┬──────┘    │
│  │ - jenkins-agent (Svc)    │   │          │                │           │
│  │   puerto 50000 JNLP      │   │   ┌──────┴──────┐  ┌──────┴──────┐    │
│  └─────────────────────────┘   │   │ product-    │  │ order-     │    │
│                                 │   │ service     │  │ service     │    │
│  Pipelines lanzan pods con:     │   │ ClusterIP   │  │ ClusterIP   │    │
│  - jnlp (conecta a 50000)       │   └──────┬──────┘  └──────┬──────┘    │
│  - maven (mvn clean install)   │          │                │           │
│                                 │   ┌──────┴──────┐  ┌──────┴──────┐    │
│                                 │   │ inventory- │  │ gateway-    │    │
│                                 │   │ service    │  │ service     │    │
│                                 │   │ ClusterIP  │  │ NodePort    │    │
│                                 │   └────────────┘  │ 30088        │    │
│                                 │                  └──────┬───────┘    │
└─────────────────────────────────┴─────────────────────────┼─────────────┘
                                                            │
                                                    http://localhost:30088
```

---

### Jenkins (namespace `jenkins`)

**Despliegue:** Helm chart oficial de Jenkins (`jenkins/jenkins`).

| Elemento | Configuración |
|----------|---------------|
| **Imagen** | `jenkins/jenkins:2.541.3-jdk21` |
| **Exposición** | `NodePort` 30080 → `http://localhost:30080` |
| **Persistencia** | PVC de 8Gi para jobs, configuración y plugins |
| **Recursos** | 500m–2 CPU, 1–2Gi RAM |
| **Plugins** | `kubernetes`, `git`, `workflow-aggregator`, `pipeline-stage-view`, `credentials-binding`, `docker-workflow` |

**Agentes Kubernetes:** Jenkins lanza pods dinámicos para ejecutar pipelines. La comunicación usa JNLP (puerto 50000). El chart crea un servicio `jenkins-agent` que expone ese puerto; el controller se conecta a `jenkins-agent.jenkins.svc.cluster.local:50000`.

**JCasC (Configuration as Code):** El parche `jcasc-patch.yaml` inyecta en el ConfigMap de Jenkins la configuración del cloud Kubernetes: URL del controller, tunnel, namespace, plantilla de pod por defecto. Así los agentes se crean en el namespace `jenkins` y se conectan correctamente.

**Archivos:** `k8s/jenkins/values.yaml`, `k8s/jenkins/jcasc-patch.yaml`, `k8s/jenkins/deploy-jenkins.bat`.

---

### Pipeline (Jenkinsfile)

El pipeline usa un **agent Kubernetes** con un pod de dos contenedores:

| Contenedor | Imagen | Función |
|------------|--------|---------|
| `jnlp` | `jenkins/inbound-agent` | Conecta con el controller vía JNLP. Variable `JENKINS_TUNNEL=jenkins-agent.jenkins.svc.cluster.local:50000` para que encuentre el tunnel. |
| `maven` | `maven:3.9-eclipse-temurin-21` | Ejecuta `mvn clean install`. Los comandos `sh` van dentro de `container('maven')`. |

**Stages:** Product Service → Inventory Service → Order Service → Gateway Service. Si alguno falla, el pipeline se detiene.

---

### Ecommerce (namespace `ecommerce`)

**Bases de datos MySQL:** Tres Deployments con PVC de 1Gi cada uno. Servicios `ClusterIP` para que solo los microservicios del namespace puedan conectarse.

| Servicio | Base de datos | Puerto interno |
|----------|---------------|----------------|
| `mysql-product` | `ms_products` | 3306 |
| `mysql-order` | `ms_orders` | 3306 |
| `mysql-inventory` | `ms_inventory` | 3306 |

**Microservicios:** Cada uno es un Deployment + Service. Los servicios de negocio usan `ClusterIP`; solo el gateway se expone con `NodePort` 30088.

| Deployment | Imagen | Puerto | Tipo Service | Variables de entorno |
|------------|--------|--------|--------------|----------------------|
| `product-service` | `product-service:latest` | 8081 | ClusterIP | `SPRING_DATASOURCE_URL=jdbc:mysql://mysql-product:3306/ms_products` |
| `order-service` | `order-service:latest` | 8082 | ClusterIP | Datasource + `SERVICES_PRODUCT_BASE_URL`, `SERVICES_INVENTORY_BASE_URL` |
| `inventory-service` | `inventory-service:latest` | 8083 | ClusterIP | Datasource + `SERVICES_PRODUCT_BASE_URL` |
| `gateway-service` | `gateway-service:latest` | 8088 | **NodePort 30088** | `SPRING_PROFILES_ACTIVE=k8s` |

**Red interna:** En el perfil `k8s`, el gateway enruta a `http://product-service:8081`, `http://order-service:8082`, `http://inventory-service:8083`. Los nombres DNS son los nombres de los Services de Kubernetes.

**Archivos:** `k8s/ecommerce/namespace.yaml`, `k8s/ecommerce/mysql-*.yaml`, `k8s/ecommerce/*-service.yaml`, `deploy-all.bat`.

---

### Docker

Cada microservicio tiene un **Dockerfile multi-stage**:

1. **Stage build:** Imagen `eclipse-temurin:21-jdk`, Maven wrapper (`mvnw`), `mvn clean package -DskipTests`.
2. **Stage run:** Imagen `eclipse-temurin:21-jre`, solo el JAR. `ENTRYPOINT ["java", "-jar", "app.jar"]`.

Las imágenes se construyen en local (`docker build`) y se usan con `imagePullPolicy: IfNotPresent` para que Kubernetes las tome del daemon de Docker (por ejemplo, Docker Desktop).

---

### Resumen de puertos y accesos

| Componente | Puerto interno | Acceso externo |
|------------|----------------|----------------|
| Jenkins | 8080 | `localhost:30080` (NodePort) |
| Jenkins agent (JNLP) | 50000 | Solo dentro del clúster |
| Gateway | 8088 | `localhost:30088` (NodePort) |
| Product, Order, Inventory | 8081, 8082, 8083 | Solo vía gateway (ClusterIP) |
| MySQL | 3306 | Solo dentro del namespace (ClusterIP) |

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

## Scripts de arranque (.bat)

| Script | Descripción |
|--------|-------------|
| `start-dev.bat` | Entorno local: levanta MySQL (si no está) y abre 4 terminales con los microservicios. Requiere Windows Terminal. |
| `stop-db.bat` | Para y elimina los contenedores MySQL de Docker Compose. |
| `k8s\ecommerce\deploy-all.bat` | Despliega la plataforma en Kubernetes: crea namespace, MySQL, construye imágenes Docker y despliega los 4 microservicios. Gateway en puerto **30088**. |
| `k8s\jenkins\deploy-jenkins.bat` | Despliega Jenkins en Kubernetes (Helm) para CI/CD. |

---

## Puesta en marcha

### Opción A: Entorno local (desarrollo)

```bash
# 1. Levantar bases de datos
docker compose up -d

# 2. Arrancar microservicios (automático con 4 pestañas)
start-dev.bat
```

O manualmente, en 4 terminales:

```bash
# Terminal 1 - Product Service
cd microservices-platform/product-service && mvnw.cmd spring-boot:run

# Terminal 2 - Inventory Service
cd microservices-platform/inventory-service && mvnw.cmd spring-boot:run

# Terminal 3 - Order Service
cd microservices-platform/order-service && mvnw.cmd spring-boot:run

# Terminal 4 - Gateway
cd microservices-platform/gateway-service && mvnw.cmd spring-boot:run
```

**URLs:** Gateway http://localhost:8088 | Productos http://localhost:8088/api/v1/products

### Opción B: Kubernetes (producción local)

```bash
k8s\ecommerce\deploy-all.bat
```

**URL:** Gateway http://localhost:30088

### Verificar que todo funciona

- **Gateway (local):** http://localhost:8088/actuator/health  
- **Gateway (K8s):** http://localhost:30088/actuator/health  
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
# o ejecutar stop-db.bat
```

---

## CI/CD

Este proyecto incluye un pipeline de **Integración Continua (CI)** y **Despliegue Continuo (CD)** usando Jenkins, Docker y Kubernetes.

### ¿Qué es CI/CD?

- **CI (Integración Continua):** Cada vez que subes código (por ejemplo con `git push`), el sistema compila el proyecto y ejecuta los tests de forma automática. Si algo falla, te avisa antes de que el error llegue a producción.
- **CD (Despliegue Continuo):** Una vez que el código está validado, se empaqueta en contenedores Docker y se despliega en Kubernetes. Así puedes tener la aplicación corriendo en un entorno similar a producción con un solo comando.

### Flujo completo

```
git push → GitHub
    ↓
Jenkins detecta el cambio
    ↓
Lanza un pod en Kubernetes con Maven
    ↓
Compila y ejecuta tests de: product-service, inventory-service, order-service, gateway-service
    ↓
Si todo pasa → Build SUCCESS
    ↓
Ejecutas deploy-all.bat (manual)
    ↓
Construye imágenes Docker de cada microservicio
    ↓
Despliega en Kubernetes (MySQL + 4 servicios)
    ↓
App disponible en http://localhost:30088
```

### Componentes

| Componente | Rol |
|------------|-----|
| **Jenkins** | Servidor de CI: escucha el repositorio, lanza el pipeline en cada push y ejecuta `mvn clean install` en cada microservicio dentro de un pod de Kubernetes. |
| **Docker** | Empaqueta cada microservicio (Java + JAR) en una imagen. El `Dockerfile` de cada servicio usa Maven para compilar y Eclipse Temurin para ejecutar. |
| **Kubernetes** | Orquesta los contenedores: crea los pods de MySQL, los servicios (product, order, inventory, gateway) y expone el gateway por NodePort 30088. |

### Cómo ponerlo en marcha

1. **Desplegar Jenkins** (una vez):
   ```bash
   k8s\jenkins\deploy-jenkins.bat
   ```
   Jenkins quedará disponible en el puerto que indique el script (por ejemplo, `kubectl port-forward` si usas clúster local).

2. **Configurar el pipeline:** Crea un job en Jenkins que apunte a tu repositorio GitHub y use el `Jenkinsfile` de la raíz del proyecto. En cada push, Jenkins ejecutará el pipeline definido ahí.

3. **Desplegar la aplicación** (cuando quieras actualizar el entorno K8s):
   ```bash
   k8s\ecommerce\deploy-all.bat
   ```
   Este script construye las imágenes Docker, las despliega en Kubernetes y deja la app accesible en http://localhost:30088.

### Archivos clave

| Archivo | Descripción |
|---------|-------------|
| `Jenkinsfile` | Define el pipeline: agent Kubernetes con Maven, stages para cada microservicio. |
| `k8s/jenkins/` | Helm values y JCasC para desplegar Jenkins en K8s. |
| `k8s/ecommerce/` | Manifiestos YAML para MySQL y los 4 microservicios. |
| `*/Dockerfile` | Cada microservicio tiene su Dockerfile multi-stage (build + run). |

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

Dos colecciones según entorno:

| Colección | Entorno | Gateway | Uso |
|-----------|---------|---------|-----|
| `Microservices-Ecommerce-Platform.postman_collection.json` | Local | http://localhost:8088 | Desarrollo con `start-dev.bat` |
| `Ecommerce-Platform-PROD.postman_collection.json` | Kubernetes | http://localhost:30088 | Despliegue con `deploy-all.bat` |

Importa la colección que corresponda. Las variables de URL ya vienen configuradas.

---

## Acceso manual a las bases de datos

Herramientas recomendadas: MySQL Workbench, DBeaver, TablePlus.

```
Host: localhost
Puertos: 3307 (products) | 3308 (orders) | 3309 (inventory)
Usuario: davidBernal
Contraseña: admin1234
```
