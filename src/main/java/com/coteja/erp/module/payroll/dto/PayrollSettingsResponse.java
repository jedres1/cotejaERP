package com.coteja.erp.module.payroll.dto;

import com.coteja.erp.module.payroll.entity.PayrollSettings;

import java.math.BigDecimal;

public record PayrollSettingsResponse(
        Long id,
        Long accountingPackageId,
        String accountingPackageName,
        Long accountSalariesId,
        String accountSalariesName,
        Long accountIsssEmployerId,
        String accountIsssEmployerName,
        Long accountAfpEmployerId,
        String accountAfpEmployerName,
        Long accountIsssPayableId,
        String accountIsssPayableName,
        Long accountAfpPayableId,
        String accountAfpPayableName,
        Long accountIsrPayableId,
        String accountIsrPayableName,
        Long accountSalariesPayableId,
        String accountSalariesPayableName,
        BigDecimal isssSalaryCap,
        BigDecimal isssEmployeeRate,
        BigDecimal isssEmployerRate,
        BigDecimal afpEmployeeRate,
        BigDecimal afpEmployerRate
) {
    public static PayrollSettingsResponse from(PayrollSettings s) {
        return new PayrollSettingsResponse(
                s.getId(),
                s.getAccountingPackage() != null ? s.getAccountingPackage().getId() : null,
                s.getAccountingPackage() != null ? s.getAccountingPackage().getName() : null,
                s.getAccountSalaries() != null ? s.getAccountSalaries().getId() : null,
                s.getAccountSalaries() != null ? s.getAccountSalaries().getName() : null,
                s.getAccountIsssEmployer() != null ? s.getAccountIsssEmployer().getId() : null,
                s.getAccountIsssEmployer() != null ? s.getAccountIsssEmployer().getName() : null,
                s.getAccountAfpEmployer() != null ? s.getAccountAfpEmployer().getId() : null,
                s.getAccountAfpEmployer() != null ? s.getAccountAfpEmployer().getName() : null,
                s.getAccountIsssPayable() != null ? s.getAccountIsssPayable().getId() : null,
                s.getAccountIsssPayable() != null ? s.getAccountIsssPayable().getName() : null,
                s.getAccountAfpPayable() != null ? s.getAccountAfpPayable().getId() : null,
                s.getAccountAfpPayable() != null ? s.getAccountAfpPayable().getName() : null,
                s.getAccountIsrPayable() != null ? s.getAccountIsrPayable().getId() : null,
                s.getAccountIsrPayable() != null ? s.getAccountIsrPayable().getName() : null,
                s.getAccountSalariesPayable() != null ? s.getAccountSalariesPayable().getId() : null,
                s.getAccountSalariesPayable() != null ? s.getAccountSalariesPayable().getName() : null,
                s.getIsssSalaryCap(), s.getIsssEmployeeRate(), s.getIsssEmployerRate(),
                s.getAfpEmployeeRate(), s.getAfpEmployerRate()
        );
    }
}
