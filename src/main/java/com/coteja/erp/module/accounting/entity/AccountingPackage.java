package com.coteja.erp.module.accounting.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accounting_packages")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountingPackage extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PackageType type = PackageType.automatico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debit_account_id")
    private AccountingAccount debitAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_account_id")
    private AccountingAccount creditAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_debit_account_id")
    private AccountingAccount secondaryDebitAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_credit_account_id")
    private AccountingAccount secondaryCreditAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    @Column(name = "last_correlative")
    @Builder.Default
    private Integer lastCorrelative = 0;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Transient
    public synchronized String reserveNextNumber() {
        lastCorrelative++;
        return String.format("%s-%04d", code, lastCorrelative);
    }

    public enum PackageType {
        automatico, manual
    }
}
