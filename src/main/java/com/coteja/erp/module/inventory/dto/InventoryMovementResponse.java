package com.coteja.erp.module.inventory.dto;

import com.coteja.erp.module.inventory.entity.InventoryMovement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        Long warehouseId,
        String warehouseName,
        String type,
        BigDecimal quantity,
        String documentType,
        String documentNumber,
        String notes,
        LocalDateTime createdAt
) {
    public static InventoryMovementResponse from(InventoryMovement m) {
        return new InventoryMovementResponse(
                m.getId(), m.getProductId(),
                m.getWarehouse() != null ? m.getWarehouse().getId() : null,
                m.getWarehouse() != null ? m.getWarehouse().getName() : null,
                m.getType() != null ? m.getType().name() : null,
                m.getQuantity(), m.getDocumentType(), m.getDocumentNumber(),
                m.getNotes(), m.getCreatedAt()
        );
    }
}
