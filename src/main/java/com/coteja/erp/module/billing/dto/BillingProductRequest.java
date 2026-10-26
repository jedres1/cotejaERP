package com.coteja.erp.module.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record BillingProductRequest(
        @NotBlank String code,
        @NotBlank String description,
        String type,
        @NotNull BigDecimal price,
        String unit,
        Long productTypeId,
        List<String> tributes,
        Boolean exempt,
        String notes
) {}
