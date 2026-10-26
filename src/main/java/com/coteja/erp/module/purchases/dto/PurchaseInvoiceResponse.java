package com.coteja.erp.module.purchases.dto;

import com.coteja.erp.module.purchases.entity.PurchaseInvoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PurchaseInvoiceResponse(
        Long id,
        Long supplierId,
        String supplierName,
        String documentType,
        String invoiceNumber,
        LocalDate purchaseDate,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal iva,
        BigDecimal total,
        String paymentMethod,
        String paymentStatus,
        String status,
        String notes,
        LocalDateTime createdAt
) {
    public static PurchaseInvoiceResponse from(PurchaseInvoice p) {
        return new PurchaseInvoiceResponse(
                p.getId(),
                p.getSupplier() != null ? p.getSupplier().getId() : null,
                p.getSupplier() != null ? p.getSupplier().getName() : p.getReceptorName(),
                p.getDocumentType(), p.getInvoiceNumber(),
                p.getPurchaseDate(), p.getDueDate(),
                p.getSubtotal(), p.getIva(), p.getTotal(),
                p.getPaymentMethod(), p.getPaymentStatus(),
                p.getStatus(), p.getNotes(), p.getCreatedAt()
        );
    }
}
