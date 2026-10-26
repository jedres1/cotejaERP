package com.coteja.erp.module.billing.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull BigDecimal amount,
        @NotNull String method,
        String reference,
        String notes,
        Long bankAccountId
) {}
