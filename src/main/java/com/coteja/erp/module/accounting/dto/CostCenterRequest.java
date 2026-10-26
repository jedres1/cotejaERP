package com.coteja.erp.module.accounting.dto;

import jakarta.validation.constraints.NotBlank;

public record CostCenterRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        Long parentId,
        Boolean isActive
) {}
