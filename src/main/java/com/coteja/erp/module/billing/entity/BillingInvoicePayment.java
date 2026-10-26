package com.coteja.erp.module.billing.entity;

import com.coteja.erp.module.banking.entity.BankAccount;
import com.coteja.erp.module.banking.entity.BankReconciliation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_invoice_payments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BillingInvoicePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_invoice_id", nullable = false)
    private BillingInvoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id")
    private BankReconciliation reconciliation;

    @Column(nullable = false)
    private BigDecimal amount;

    private String method;
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "registered_at", nullable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "is_reconciled")
    @Builder.Default
    private boolean reconciled = false;
}
