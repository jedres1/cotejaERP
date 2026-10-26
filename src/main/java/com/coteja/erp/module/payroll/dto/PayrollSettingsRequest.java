package com.coteja.erp.module.payroll.dto;

import java.math.BigDecimal;

public record PayrollSettingsRequest(
        Long accountingPackageId,
        Long accountSalariesId,
        Long accountIsssEmployerId,
        Long accountAfpEmployerId,
        Long accountIsssPayableId,
        Long accountAfpPayableId,
        Long accountIsrPayableId,
        Long accountSalariesPayableId,
        BigDecimal isssSalaryCap,
        BigDecimal isssEmployeeRate,
        BigDecimal isssEmployerRate,
        BigDecimal afpEmployeeRate,
        BigDecimal afpEmployerRate
) {}
