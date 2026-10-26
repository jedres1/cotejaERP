package com.coteja.erp.module.banking.dto;

import com.coteja.erp.module.banking.entity.BankReconciliation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankReconciliationResponse(
        Long id,
        Long bankAccountId,
        String bankAccountName,
        Integer periodYear,
        Integer periodMonth,
        String periodLabel,
        BigDecimal statementBalance,
        String status,
        LocalDateTime completedAt,
        String notes
) {
    public static BankReconciliationResponse from(BankReconciliation r) {
        return new BankReconciliationResponse(
                r.getId(),
                r.getBankAccount() != null ? r.getBankAccount().getId() : null,
                r.getBankAccount() != null ? r.getBankAccount().getName() : null,
                r.getPeriodYear(), r.getPeriodMonth(),
                r.getPeriodLabel(),
                r.getStatementBalance(), r.getStatus(),
                r.getCompletedAt(), r.getNotes()
        );
    }
}
