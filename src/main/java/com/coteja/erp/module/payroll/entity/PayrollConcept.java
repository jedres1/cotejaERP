package com.coteja.erp.module.payroll.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.accounting.entity.AccountingAccount;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payroll_concepts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PayrollConcept extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConceptType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false)
    @Builder.Default
    private CalculationMethod calculationMethod = CalculationMethod.fijo;

    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String formula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountingAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payable_account_id")
    private AccountingAccount payableAccount;

    @Column(name = "system_key")
    private String systemKey;

    @Column(name = "applies_to_all")
    @Builder.Default
    private boolean appliesToAll = true;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    public enum ConceptType { beneficio, deduccion }
    public enum CalculationMethod { fijo, porcentaje, formula, editable }
}
