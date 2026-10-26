package com.coteja.erp.module.banking.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_accounts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BankAccount extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_type")
    private String accountType;

    @Builder.Default
    private String currency = "USD";

    @Column(name = "opening_balance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "bankAccount", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BankTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "bankAccount", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BankReconciliation> reconciliations = new ArrayList<>();
}
