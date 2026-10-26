package com.coteja.erp.module.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        String code,
        String address,
        String phone,
        String contactName,
        Boolean isActive
) {}
