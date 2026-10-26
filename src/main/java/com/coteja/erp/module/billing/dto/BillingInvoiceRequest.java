package com.coteja.erp.module.billing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BillingInvoiceRequest(
        @NotNull String documentType,
        @NotNull LocalDate issuedAt,
        Long clientId,
        String customerName,
        @NotNull @Valid List<LineRequest> lines,
        String observations
) {
    public record LineRequest(
            @NotNull Long productId,
            @NotNull BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal discount,
            String description
    ) {}
}
