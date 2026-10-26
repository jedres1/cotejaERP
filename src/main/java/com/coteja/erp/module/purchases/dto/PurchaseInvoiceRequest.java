package com.coteja.erp.module.purchases.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseInvoiceRequest(
        Long supplierId,
        String documentType,
        @NotNull(message = "El número de factura es obligatorio")
        String invoiceNumber,
        @NotNull
        LocalDate purchaseDate,
        LocalDate dueDate,
        @NotNull @Positive
        BigDecimal subtotal,
        BigDecimal iva,
        @NotNull @Positive
        BigDecimal total,
        String paymentMethod,
        String notes
) {}
