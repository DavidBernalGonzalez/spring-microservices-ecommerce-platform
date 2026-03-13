package com.microservice.inventory.enums;

public enum InventoryMovementType {
    IN, // Entrada stock (ORDER)
    OUT, // Salida stock (
    RESERVE, // Se bloquea stock para una orden pendiente
    RELEASE // Se libera stock bloqueado por una orden cancelada o modificada
}