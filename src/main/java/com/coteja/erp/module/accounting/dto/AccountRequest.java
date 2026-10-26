package com.coteja.erp.module.accounting.dto;

import com.coteja.erp.module.accounting.entity.AccountingAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull AccountingAccount.AccountType type,
        @NotNull AccountingAccount.AccountNature nature,
        Long parentId,
        Integer level,
        Boolean isActive,
        String notes
) {}
