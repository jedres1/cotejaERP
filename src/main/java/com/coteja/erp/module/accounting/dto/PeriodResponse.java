package com.coteja.erp.module.accounting.dto;

import com.coteja.erp.module.accounting.entity.AccountingPeriod;

import java.time.LocalDateTime;

public record PeriodResponse(
        Long id,
        Integer year,
        Integer month,
        String name,
        String status,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
    public static PeriodResponse from(AccountingPeriod p) {
        return new PeriodResponse(p.getId(), p.getYear(), p.getMonth(),
                p.getName(), p.getStatus(), p.getOpenedAt(), p.getClosedAt());
    }
}
