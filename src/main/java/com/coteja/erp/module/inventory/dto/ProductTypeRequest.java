package com.coteja.erp.module.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductTypeRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        String code,
        Boolean controlsInventory,
        Boolean isActive
) {}
