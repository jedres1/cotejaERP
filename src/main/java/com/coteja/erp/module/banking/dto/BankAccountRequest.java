package com.coteja.erp.module.banking.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record BankAccountRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        String bankName,
        String accountNumber,
        String accountType,
        String currency,
        BigDecimal openingBalance,
        Boolean isActive,
        String notes
) {}
