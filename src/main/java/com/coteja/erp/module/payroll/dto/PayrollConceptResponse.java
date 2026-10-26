package com.coteja.erp.module.payroll.dto;

import com.coteja.erp.module.payroll.entity.PayrollConcept;

import java.math.BigDecimal;

public record PayrollConceptResponse(
        Long id,
        String name,
        String type,
        String calculationMethod,
        BigDecimal amount,
        BigDecimal rate,
        String formula,
        Long accountId,
        String accountName,
        Long payableAccountId,
        String payableAccountName,
        String systemKey,
        boolean appliesToAll,
        boolean isActive
) {
    public static PayrollConceptResponse from(PayrollConcept c) {
        return new PayrollConceptResponse(
                c.getId(), c.getName(),
                c.getType() != null ? c.getType().name() : null,
                c.getCalculationMethod() != null ? c.getCalculationMethod().name() : null,
                c.getAmount(), c.getRate(), c.getFormula(),
                c.getAccount() != null ? c.getAccount().getId() : null,
                c.getAccount() != null ? c.getAccount().getName() : null,
                c.getPayableAccount() != null ? c.getPayableAccount().getId() : null,
                c.getPayableAccount() != null ? c.getPayableAccount().getName() : null,
                c.getSystemKey(), c.isAppliesToAll(), c.isActive()
        );
    }
}
