package com.coteja.erp.module.payroll.dto;

import com.coteja.erp.module.payroll.entity.PayrollPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PayrollPeriodResponse(
        Long id,
        String name,
        LocalDate periodStart,
        LocalDate periodEnd,
        String status,
        String notes,
        LocalDateTime appliedAt,
        BigDecimal totalGross,
        BigDecimal totalNet,
        BigDecimal totalEmployerCost,
        List<PayrollLineResponse> lines
) {
    public static PayrollPeriodResponse from(PayrollPeriod p) {
        List<PayrollLineResponse> lines = p.getLines() == null ? List.of() :
                p.getLines().stream().map(PayrollLineResponse::from).toList();

        BigDecimal totalGross = lines.stream()
                .map(PayrollLineResponse::grossSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = lines.stream()
                .map(PayrollLineResponse::netSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = lines.stream()
                .map(PayrollLineResponse::totalEmployerCost).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PayrollPeriodResponse(
                p.getId(), p.getName(), p.getPeriodStart(), p.getPeriodEnd(),
                p.getStatus() != null ? p.getStatus().name() : null,
                p.getNotes(), p.getAppliedAt(),
                totalGross, totalNet, totalCost, lines
        );
    }
}
