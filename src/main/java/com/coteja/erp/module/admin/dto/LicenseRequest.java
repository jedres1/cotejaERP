package com.coteja.erp.module.admin.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LicenseRequest(
        @NotNull(message = "La empresa es obligatoria")
        Long companyId,
        @NotNull(message = "El plan es obligatorio")
        Long planId,
        String licenseType,
        String status,
        @NotNull
        LocalDate startsAt,
        @NotNull
        LocalDate expiresAt,
        Integer maxUsers,
        Integer maxDevices,
        Integer graceDays
) {}
