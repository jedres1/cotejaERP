package com.coteja.erp.module.inventory.dto;

import com.coteja.erp.module.inventory.entity.ProductType;

public record ProductTypeResponse(
        Long id,
        String name,
        String code,
        boolean controlsInventory,
        boolean isActive
) {
    public static ProductTypeResponse from(ProductType pt) {
        return new ProductTypeResponse(pt.getId(), pt.getName(), pt.getCode(),
                pt.isControlsInventory(), pt.isActive());
    }
}
