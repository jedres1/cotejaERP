package com.coteja.erp.module.accounting.dto;

import com.coteja.erp.module.accounting.entity.AccountingPackage;

public record PackageResponse(
        Long id,
        String code,
        String name,
        String description,
        String type,
        Long debitAccountId,
        String debitAccountName,
        Long creditAccountId,
        String creditAccountName,
        Long secondaryDebitAccountId,
        String secondaryDebitAccountName,
        Long secondaryCreditAccountId,
        String secondaryCreditAccountName,
        Long costCenterId,
        String costCenterName,
        Integer lastCorrelative,
        boolean isActive
) {
    public static PackageResponse from(AccountingPackage p) {
        return new PackageResponse(
                p.getId(), p.getCode(), p.getName(), p.getDescription(),
                p.getType() != null ? p.getType().name() : null,
                p.getDebitAccount() != null ? p.getDebitAccount().getId() : null,
                p.getDebitAccount() != null ? p.getDebitAccount().getName() : null,
                p.getCreditAccount() != null ? p.getCreditAccount().getId() : null,
                p.getCreditAccount() != null ? p.getCreditAccount().getName() : null,
                p.getSecondaryDebitAccount() != null ? p.getSecondaryDebitAccount().getId() : null,
                p.getSecondaryDebitAccount() != null ? p.getSecondaryDebitAccount().getName() : null,
                p.getSecondaryCreditAccount() != null ? p.getSecondaryCreditAccount().getId() : null,
                p.getSecondaryCreditAccount() != null ? p.getSecondaryCreditAccount().getName() : null,
                p.getCostCenter() != null ? p.getCostCenter().getId() : null,
                p.getCostCenter() != null ? p.getCostCenter().getName() : null,
                p.getLastCorrelative(), p.isActive()
        );
    }
}
