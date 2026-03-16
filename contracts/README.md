# API Contracts

Contratos OpenAPI que definen las APIs consumidas entre microservicios.

## Estructura

| Archivo | Descripción | Consumidor |
|---------|-------------|------------|
| `product-api.yaml` | GET /api/v1/products/{id} | order-service |
| `inventory-api.yaml` | POST reserve, POST release | order-service |

## Uso

- **Fuente de verdad:** Si Product o Inventory cambian la API, estos contratos deben actualizarse.
- **Validación:** Los tests de contrato en order-service verifican que el consumidor funciona con respuestas que cumplen el contrato.
- **Documentación:** Sirven como documentación compartida de la API.

## Flujo

1. **Producer** (product-service, inventory-service) implementa la API definida en el contrato.
2. **Consumer** (order-service) consume la API y sus tests usan stubs que devuelven respuestas conforme al contrato.
3. Si el producer cambia algo que rompe el contrato, los tests del consumer fallan.
