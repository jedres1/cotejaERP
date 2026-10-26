package com.coteja.erp.module.banking.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bank_transactions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BankTransaction extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id")
    private BankReconciliation reconciliation;

    @Column(nullable = false)
    private String type;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "bank_account")
    private String bankAccountRef;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private String reference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_reconciled")
    @Builder.Default
    private boolean reconciled = false;
}
