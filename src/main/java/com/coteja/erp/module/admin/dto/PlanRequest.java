package com.coteja.erp.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlanRequest(
        @NotBlank(message = "El código es obligatorio")
        String code,
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        @NotNull
        BigDecimal monthlyPrice,
        @NotNull
        BigDecimal annualPrice,
        BigDecimal implementationFee,
        BigDecimal additionalUserPrice,
        Integer includedUsers,
        Integer maxDevices,
        Boolean supportIncluded,
        Boolean cloudBackup,
        Boolean unlimitedDocuments,
        Boolean isActive
) {}
