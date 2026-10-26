package com.coteja.erp.module.accounting.dto;

import com.coteja.erp.module.accounting.entity.JournalEntry;
import com.coteja.erp.module.accounting.entity.JournalEntryLine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JournalEntryResponse(
        Long id,
        String entryNumber,
        LocalDate entryDate,
        String description,
        String reference,
        String status,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        boolean isBalanced,
        String packageCode,
        String sourceType,
        Long sourceId,
        String sourceDocument,
        Long approvedById,
        LocalDateTime approvedAt,
        String notes,
        List<LineResponse> lines,
        LocalDateTime createdAt
) {
    public record LineResponse(
            Long id,
            Long accountId,
            String accountCode,
            String accountName,
            Long costCenterId,
            String costCenterName,
            String description,
            BigDecimal debit,
            BigDecimal credit,
            Integer sortOrder
    ) {
        public static LineResponse from(JournalEntryLine l) {
            return new LineResponse(
                    l.getId(),
                    l.getAccount() != null ? l.getAccount().getId() : null,
                    l.getAccount() != null ? l.getAccount().getCode() : null,
                    l.getAccount() != null ? l.getAccount().getName() : null,
                    l.getCostCenter() != null ? l.getCostCenter().getId() : null,
                    l.getCostCenter() != null ? l.getCostCenter().getName() : null,
                    l.getDescription(), l.getDebit(), l.getCredit(), l.getSortOrder()
            );
        }
    }

    public static JournalEntryResponse from(JournalEntry e) {
        List<LineResponse> lines = e.getLines() == null ? List.of() :
                e.getLines().stream().map(LineResponse::from).toList();
        return new JournalEntryResponse(
                e.getId(), e.getEntryNumber(), e.getEntryDate(), e.getDescription(),
                e.getReference(), e.getStatus() != null ? e.getStatus().name() : null,
                e.getTotalDebit(), e.getTotalCredit(), e.isBalanced(),
                e.getAccountingPackage() != null ? e.getAccountingPackage().getCode() : null,
                e.getSourceType(), e.getSourceId(), e.getSourceDocument(),
                e.getApprovedBy() != null ? e.getApprovedBy().getId() : null,
                e.getApprovedAt(), e.getNotes(), lines, e.getCreatedAt()
        );
    }
}
