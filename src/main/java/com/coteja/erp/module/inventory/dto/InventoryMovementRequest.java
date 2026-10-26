package com.coteja.erp.module.inventory.dto;

import com.coteja.erp.module.inventory.entity.InventoryMovement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InventoryMovementRequest(
        @NotNull Long productId,
        @NotNull Long warehouseId,
        @NotNull InventoryMovement.MovementType type,
        @NotNull @Positive BigDecimal quantity,
        String documentType,
        String documentNumber,
        String notes
) {}
