package com.coteja.erp.module.accounting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record JournalEntryRequest(
        @NotNull LocalDate entryDate,
        @NotBlank String description,
        String reference,
        Long accountingPackageId,
        String notes,
        @NotNull @Valid List<LineRequest> lines
) {
    public record LineRequest(
            @NotNull Long accountId,
            Long costCenterId,
            String description,
            @NotNull BigDecimal debit,
            @NotNull BigDecimal credit,
            Integer sortOrder
    ) {}
}
