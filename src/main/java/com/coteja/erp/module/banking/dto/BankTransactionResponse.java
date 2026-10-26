package com.coteja.erp.module.banking.dto;

import com.coteja.erp.module.banking.entity.BankTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BankTransactionResponse(
        Long id,
        Long bankAccountId,
        String bankAccountName,
        String type,
        LocalDate transactionDate,
        BigDecimal amount,
        String reference,
        String notes,
        boolean isReconciled,
        LocalDateTime createdAt
) {
    public static BankTransactionResponse from(BankTransaction t) {
        return new BankTransactionResponse(
                t.getId(),
                t.getBankAccount() != null ? t.getBankAccount().getId() : null,
                t.getBankAccount() != null ? t.getBankAccount().getName() : null,
                t.getType(), t.getTransactionDate(), t.getAmount(),
                t.getReference(), t.getNotes(), t.isReconciled(), t.getCreatedAt()
        );
    }
}
