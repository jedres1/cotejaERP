package com.coteja.erp.module.inventory.dto;

import com.coteja.erp.module.inventory.entity.Warehouse;

public record WarehouseResponse(
        Long id,
        String name,
        String code,
        String address,
        String phone,
        String contactName,
        boolean isActive
) {
    public static WarehouseResponse from(Warehouse w) {
        return new WarehouseResponse(w.getId(), w.getName(), w.getCode(),
                w.getAddress(), w.getPhone(), w.getContactName(), w.isActive());
    }
}
