# 🐳 Microservices Platform – Databases Setup

Este proyecto utiliza **Docker Compose** para levantar las bases de datos necesarias para los microservicios.

Cada microservicio tiene su **propia base de datos MySQL**, siguiendo buenas prácticas de arquitectura de **microservicios**:
`database per service`.

---

# 🗄️ Bases de datos incluidas

El archivo `docker-compose.yml` levanta **3 contenedores MySQL**:

| Servicio       | Base de datos  | Puerto local |
| -------------- | -------------- | ------------ |
| `db-products`  | `ms_products`  | `3307`       |
| `db-orders`    | `ms_orders`    | `3308`       |
| `db-inventory` | `ms_inventory` | `3309`       |

Cada contenedor utiliza su propio **volumen Docker** para persistir los datos.

---

# ⚙️ Requisitos

Antes de arrancar el proyecto necesitas tener instalado:

* 🐳 Docker
* 📦 Docker Compose

Puedes comprobar que están instalados ejecutando:

```bash
docker --version
docker compose version
```

---

# 🚀 Arrancar las bases de datos

Desde la carpeta donde está el archivo `docker-compose.yml` ejecuta:

```bash
docker compose up -d
```

Este comando levantará los tres contenedores **en segundo plano** (`-d` = *detached mode*).

---

# 🔍 Verificar que los contenedores están activos

```bash
docker ps
```

Deberías ver algo parecido a:

```
db-products
db-orders
db-inventory
```

---

# 🛑 Parar los contenedores

```bash
docker compose down
```

Los datos **no se perderán**, ya que se guardan en **volúmenes Docker**.

---

# 🔗 Conexión desde los microservicios

Cada microservicio se conecta a su propia base de datos usando `Spring Boot`.

### 📦 product-service

```
spring.datasource.url=jdbc:mysql://localhost:3307/ms_products
spring.datasource.username=davidBernal
spring.datasource.password=admin1234
```

### 📦 order-service

```
spring.datasource.url=jdbc:mysql://localhost:3308/ms_orders
spring.datasource.username=davidBernal
spring.datasource.password=admin1234
```

### 📦 inventory-service

```
spring.datasource.url=jdbc:mysql://localhost:3309/ms_inventory
spring.datasource.username=davidBernal
spring.datasource.password=admin1234
```

---

# 💾 Volúmenes Docker

Los datos se almacenan en los siguientes volúmenes:

* `products-data`
* `orders-data`
* `inventory-data`

Esto permite mantener los datos **aunque los contenedores se eliminen**.

---

# 🧰 Acceso manual a las bases de datos

Puedes conectarte usando herramientas como:

* 🛠️ MySQL Workbench
* 🛠️ DBeaver
* 🛠️ TablePlus

Configuración de ejemplo:

```
Host: localhost
Port: 3307 / 3308 / 3309
User: davidBernal
Password: admin1234
```

---

# 🏗️ Arquitectura de bases de datos

Cada microservicio gestiona su propia base de datos:

```
product-service   → ms_products
order-service     → ms_orders
inventory-service → ms_inventory
```

Este enfoque evita **acoplamiento entre microservicios** y facilita la **escalabilidad del sistema**.
