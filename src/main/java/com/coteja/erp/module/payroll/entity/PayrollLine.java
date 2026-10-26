package com.coteja.erp.module.payroll.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "payroll_lines")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PayrollLine extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod payrollPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Builder.Default
    private BigDecimal salary = BigDecimal.ZERO;

    @Column(name = "overtime_hours")
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "overtime_amount")
    @Builder.Default
    private BigDecimal overtimeAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal bonuses = BigDecimal.ZERO;

    @Column(name = "gross_salary", nullable = false)
    @Builder.Default
    private BigDecimal grossSalary = BigDecimal.ZERO;

    @Column(name = "isss_employee")
    @Builder.Default
    private BigDecimal isssEmployee = BigDecimal.ZERO;

    @Column(name = "afp_employee")
    @Builder.Default
    private BigDecimal afpEmployee = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal isr = BigDecimal.ZERO;

    @Column(name = "total_deductions")
    @Builder.Default
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_salary", nullable = false)
    @Builder.Default
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Column(name = "isss_employer")
    @Builder.Default
    private BigDecimal isssEmployer = BigDecimal.ZERO;

    @Column(name = "afp_employer")
    @Builder.Default
    private BigDecimal afpEmployer = BigDecimal.ZERO;

    @Column(name = "total_employer_cost")
    @Builder.Default
    private BigDecimal totalEmployerCost = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "concept_details", columnDefinition = "JSON")
    private Map<String, Object> conceptDetails;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
