package com.coteja.erp.module.admin.dto;

import com.coteja.erp.module.admin.entity.Plan;

import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String code,
        String name,
        BigDecimal monthlyPrice,
        BigDecimal annualPrice,
        BigDecimal implementationFee,
        BigDecimal additionalUserPrice,
        Integer includedUsers,
        Integer maxDevices,
        boolean supportIncluded,
        boolean cloudBackup,
        boolean unlimitedDocuments,
        boolean isActive
) {
    public static PlanResponse from(Plan p) {
        return new PlanResponse(
                p.getId(), p.getCode(), p.getName(),
                p.getMonthlyPrice(), p.getAnnualPrice(),
                p.getImplementationFee(), p.getAdditionalUserPrice(),
                p.getIncludedUsers(), p.getMaxDevices(),
                p.isSupportIncluded(), p.isCloudBackup(),
                p.isUnlimitedDocuments(), p.isActive()
        );
    }
}
