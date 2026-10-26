package com.coteja.erp.module.payroll.entity;

import com.coteja.erp.module.accounting.entity.AccountingAccount;
import com.coteja.erp.module.accounting.entity.AccountingPackage;
import com.coteja.erp.module.admin.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payroll_settings")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PayrollSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_package_id")
    private AccountingPackage accountingPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_salaries_id")
    private AccountingAccount accountSalaries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_isss_employer_id")
    private AccountingAccount accountIsssEmployer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_afp_employer_id")
    private AccountingAccount accountAfpEmployer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_isss_payable_id")
    private AccountingAccount accountIsssPayable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_afp_payable_id")
    private AccountingAccount accountAfpPayable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_isr_payable_id")
    private AccountingAccount accountIsrPayable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_salaries_payable_id")
    private AccountingAccount accountSalariesPayable;

    @Column(name = "isss_salary_cap")
    @Builder.Default
    private BigDecimal isssSalaryCap = new BigDecimal("1000.00");

    @Column(name = "isss_employee_rate")
    @Builder.Default
    private BigDecimal isssEmployeeRate = new BigDecimal("0.0300");

    @Column(name = "isss_employer_rate")
    @Builder.Default
    private BigDecimal isssEmployerRate = new BigDecimal("0.0750");

    @Column(name = "afp_employee_rate")
    @Builder.Default
    private BigDecimal afpEmployeeRate = new BigDecimal("0.0725");

    @Column(name = "afp_employer_rate")
    @Builder.Default
    private BigDecimal afpEmployerRate = new BigDecimal("0.0875");
}
