package com.coteja.erp.module.payroll.dto;

import com.coteja.erp.module.payroll.entity.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank String code,
        @NotBlank String name,
        String email,
        String dui,
        String isssNumber,
        String nup,
        Employee.AfpType afp,
        String position,
        String department,
        @NotNull LocalDate hireDate,
        @NotNull BigDecimal salary,
        String bankName,
        String bankAccount,
        Boolean isActive
) {}
