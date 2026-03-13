package com.microservice.inventory.enums;

public enum InventoryReferenceType {
    ORDER,          // movimiento generado por un pedido
    RESTOCK,        // reposición de almacén / proveedor
    MANUAL_ADJUSTMENT, // corrección manual de stock
    INITIAL_STOCK   // stock inicial al crear inventario
}		