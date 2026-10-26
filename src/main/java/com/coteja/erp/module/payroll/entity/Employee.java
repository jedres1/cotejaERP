package com.coteja.erp.module.payroll.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Employee extends AuditableEntity {

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

    private String email;
    private String dui;

    @Column(name = "isss_number")
    private String isssNumber;

    private String nup;

    @Enumerated(EnumType.STRING)
    private AfpType afp;

    private String position;
    private String department;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal salary = BigDecimal.ZERO;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @ManyToMany
    @JoinTable(name = "employee_payroll_concept",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "payroll_concept_id"))
    @Builder.Default
    private Set<PayrollConcept> concepts = new HashSet<>();

    public enum AfpType {
        crecer, confia
    }
}
