package com.coteja.erp.module.billing.dto;

import com.coteja.erp.module.billing.entity.BillingInvoice;
import com.coteja.erp.module.billing.entity.BillingInvoicePayment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BillingInvoiceResponse(
        Long id,
        String numberControl,
        String generationCode,
        String documentType,
        LocalDate issuedAt,
        String customerName,
        Long clientId,
        BigDecimal subtotal,
        BigDecimal iva,
        BigDecimal total,
        BigDecimal balance,
        String status,
        boolean accepted,
        boolean hasError,
        boolean emailSent,
        String paymentStatus,
        BigDecimal amountPaid,
        LocalDateTime paidAt,
        String receptionStamp,
        String observations,
        LocalDateTime createdAt,
        List<PaymentResponse> payments
) {
    public record PaymentResponse(
            Long id,
            BigDecimal amount,
            String method,
            String reference,
            LocalDateTime registeredAt,
            boolean reconciled
    ) {
        public static PaymentResponse from(BillingInvoicePayment p) {
            return new PaymentResponse(p.getId(), p.getAmount(), p.getMethod(),
                    p.getReference(), p.getRegisteredAt(), p.isReconciled());
        }
    }

    public static BillingInvoiceResponse from(BillingInvoice i) {
        List<PaymentResponse> payments = i.getPayments() == null ? List.of() :
                i.getPayments().stream().map(PaymentResponse::from).toList();
        return new BillingInvoiceResponse(
                i.getId(), i.getNumberControl(), i.getGenerationCode(),
                i.getDocumentType(), i.getIssuedAt(),
                i.getCustomerName(),
                i.getClient() != null ? i.getClient().getId() : null,
                i.getSubtotal(), i.getIva(), i.getTotal(), i.getBalance(),
                i.getStatus(), i.isAccepted(), i.isHasError(), i.isEmailSent(),
                i.getPaymentStatus(), i.getAmountPaid(), i.getPaidAt(),
                i.getReceptionStamp(), i.getObservations(), i.getCreatedAt(),
                payments
        );
    }
}
