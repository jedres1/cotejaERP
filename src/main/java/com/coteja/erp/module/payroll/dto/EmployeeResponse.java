package com.coteja.erp.module.payroll.dto;

import com.coteja.erp.module.payroll.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String code,
        String name,
        String email,
        String dui,
        String isssNumber,
        String nup,
        String afp,
        String position,
        String department,
        LocalDate hireDate,
        BigDecimal salary,
        String bankName,
        String bankAccount,
        boolean isActive
) {
    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(
                e.getId(), e.getCode(), e.getName(), e.getEmail(),
                e.getDui(), e.getIsssNumber(), e.getNup(),
                e.getAfp() != null ? e.getAfp().name() : null,
                e.getPosition(), e.getDepartment(), e.getHireDate(),
                e.getSalary(), e.getBankName(), e.getBankAccount(), e.isActive()
        );
    }
}
