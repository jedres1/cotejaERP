package com.coteja.erp.module.accounting.dto;

import com.coteja.erp.module.accounting.entity.AccountingPackage;
import jakarta.validation.constraints.NotBlank;

public record PackageRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        AccountingPackage.PackageType type,
        Long debitAccountId,
        Long creditAccountId,
        Long secondaryDebitAccountId,
        Long secondaryCreditAccountId,
        Long costCenterId,
        Boolean isActive
) {}
