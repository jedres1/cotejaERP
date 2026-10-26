package com.coteja.erp.module.accounting.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounting_accounts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountingAccount extends AuditableEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountNature nature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AccountingAccount parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<AccountingAccount> children = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    public static AccountType typeFromCode(String code) {
        if (code == null || code.isEmpty()) return AccountType.activo;
        return switch (code.charAt(0)) {
            case '1' -> AccountType.activo;
            case '2' -> AccountType.pasivo;
            case '3' -> AccountType.patrimonio;
            case '4' -> AccountType.gasto;
            case '5' -> AccountType.ingreso;
            default -> AccountType.activo;
        };
    }

    public static AccountNature natureFromType(AccountType type) {
        return switch (type) {
            case activo, gasto -> AccountNature.deudora;
            case pasivo, patrimonio, ingreso -> AccountNature.acreedora;
        };
    }

    public enum AccountType {
        activo, pasivo, patrimonio, gasto, ingreso
    }

    public enum AccountNature {
        deudora, acreedora
    }
}
