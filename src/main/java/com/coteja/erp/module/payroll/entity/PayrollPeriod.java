package com.coteja.erp.module.payroll.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.accounting.entity.JournalEntry;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import com.coteja.erp.module.admin.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll_periods")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PayrollPeriod extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PeriodStatus status = PeriodStatus.borrador;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id")
    private JournalEntry journalEntry;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_by")
    private User appliedBy;

    @OneToMany(mappedBy = "payrollPeriod", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PayrollLine> lines = new ArrayList<>();

    public boolean isDraft() {
        return PeriodStatus.borrador.equals(status);
    }

    public enum PeriodStatus { borrador, aplicado }
}
