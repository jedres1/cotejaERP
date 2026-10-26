package com.coteja.erp.module.payroll.dto;

import com.coteja.erp.module.payroll.entity.PayrollLine;

import java.math.BigDecimal;
import java.util.Map;

public record PayrollLineResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        BigDecimal salary,
        BigDecimal overtimeHours,
        BigDecimal overtimeAmount,
        BigDecimal bonuses,
        BigDecimal grossSalary,
        BigDecimal isssEmployee,
        BigDecimal afpEmployee,
        BigDecimal isr,
        BigDecimal totalDeductions,
        BigDecimal netSalary,
        BigDecimal isssEmployer,
        BigDecimal afpEmployer,
        BigDecimal totalEmployerCost,
        Map<String, Object> conceptDetails
) {
    public static PayrollLineResponse from(PayrollLine l) {
        return new PayrollLineResponse(
                l.getId(),
                l.getEmployee() != null ? l.getEmployee().getId() : null,
                l.getEmployee() != null ? l.getEmployee().getCode() : null,
                l.getEmployee() != null ? l.getEmployee().getName() : null,
                l.getSalary(), l.getOvertimeHours(), l.getOvertimeAmount(),
                l.getBonuses(), l.getGrossSalary(),
                l.getIsssEmployee(), l.getAfpEmployee(), l.getIsr(),
                l.getTotalDeductions(), l.getNetSalary(),
                l.getIsssEmployer(), l.getAfpEmployer(), l.getTotalEmployerCost(),
                l.getConceptDetails()
        );
    }
}
