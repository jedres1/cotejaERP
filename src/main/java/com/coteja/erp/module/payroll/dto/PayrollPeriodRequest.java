package com.coteja.erp.module.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PayrollPeriodRequest(
        @NotBlank String name,
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd,
        String notes
) {}
