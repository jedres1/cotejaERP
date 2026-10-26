package com.coteja.erp.module.banking.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Entity
@Table(name = "bank_reconciliations")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BankReconciliation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "statement_file_path")
    private String statementFilePath;

    @Column(name = "statement_balance", precision = 15, scale = 2)
    private BigDecimal statementBalance;

    @Builder.Default
    private String status = "open";

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public String getPeriodLabel() {
        String monthName = Month.of(periodMonth)
                .getDisplayName(TextStyle.FULL, new Locale("es", "SV"));
        return capitalize(monthName) + " " + periodYear;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
