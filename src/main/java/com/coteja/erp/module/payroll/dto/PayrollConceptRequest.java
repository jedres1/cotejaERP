package com.coteja.erp.module.payroll.dto;

import com.coteja.erp.module.payroll.entity.PayrollConcept;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayrollConceptRequest(
        @NotBlank String name,
        @NotNull PayrollConcept.ConceptType type,
        @NotNull PayrollConcept.CalculationMethod calculationMethod,
        BigDecimal amount,
        BigDecimal rate,
        String formula,
        Long accountId,
        Long payableAccountId,
        String systemKey,
        Boolean appliesToAll,
        Boolean isActive
) {}
