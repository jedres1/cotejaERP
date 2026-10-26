package com.coteja.erp.module.banking.dto;

import com.coteja.erp.module.banking.entity.BankAccount;

import java.math.BigDecimal;

public record BankAccountResponse(
        Long id,
        String name,
        String bankName,
        String accountNumber,
        String accountType,
        String currency,
        BigDecimal openingBalance,
        boolean isActive,
        String notes
) {
    public static BankAccountResponse from(BankAccount a) {
        return new BankAccountResponse(a.getId(), a.getName(), a.getBankName(),
                a.getAccountNumber(), a.getAccountType(), a.getCurrency(),
                a.getOpeningBalance(), a.isActive(), a.getNotes());
    }
}
