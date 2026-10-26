package com.coteja.erp.module.banking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BankReconciliationRequest(
        @NotNull Long bankAccountId,
        @NotNull @Min(2020) Integer periodYear,
        @NotNull @Min(1) @Max(12) Integer periodMonth,
        BigDecimal statementBalance,
        String notes
) {}
