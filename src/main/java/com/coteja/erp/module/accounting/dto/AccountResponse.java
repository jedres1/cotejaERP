package com.coteja.erp.module.accounting.dto;

import com.coteja.erp.module.accounting.entity.AccountingAccount;

import java.util.List;

public record AccountResponse(
        Long id,
        String code,
        String name,
        String type,
        String nature,
        Long parentId,
        String parentName,
        Integer level,
        boolean isActive,
        boolean isLeaf,
        List<AccountResponse> children
) {
    public static AccountResponse from(AccountingAccount a) {
        return new AccountResponse(
                a.getId(), a.getCode(), a.getName(),
                a.getType() != null ? a.getType().name() : null,
                a.getNature() != null ? a.getNature().name() : null,
                a.getParent() != null ? a.getParent().getId() : null,
                a.getParent() != null ? a.getParent().getName() : null,
                a.getLevel(), a.isActive(), a.isLeaf(), null
        );
    }

    public static AccountResponse fromWithChildren(AccountingAccount a) {
        List<AccountResponse> kids = a.getChildren() == null ? List.of() :
                a.getChildren().stream().map(AccountResponse::fromWithChildren).toList();
        return new AccountResponse(
                a.getId(), a.getCode(), a.getName(),
                a.getType() != null ? a.getType().name() : null,
                a.getNature() != null ? a.getNature().name() : null,
                a.getParent() != null ? a.getParent().getId() : null,
                a.getParent() != null ? a.getParent().getName() : null,
                a.getLevel(), a.isActive(), kids.isEmpty(), kids
        );
    }
}
