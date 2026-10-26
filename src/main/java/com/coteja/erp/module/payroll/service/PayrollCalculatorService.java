package com.coteja.erp.module.payroll.service;

import com.coteja.erp.module.payroll.entity.Employee;
import com.coteja.erp.module.payroll.entity.PayrollLine;
import com.coteja.erp.module.payroll.entity.PayrollSettings;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Calculates El Salvador payroll deductions: ISSS, AFP, and ISR
 * per Ministerio de Hacienda ISR monthly table.
 */
@Service
public class PayrollCalculatorService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SCALE = 2;

    public PayrollLine calculate(Employee employee, PayrollSettings settings,
                                 BigDecimal overtimeHours, BigDecimal bonuses) {

        BigDecimal salary = employee.getSalary();
        BigDecimal overtimeRate = salary.divide(new BigDecimal("240"), 4, RoundingMode.HALF_UP);
        BigDecimal overtimeAmount = overtimeRate.multiply(overtimeHours != null ? overtimeHours : BigDecimal.ZERO)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal grossSalary = salary
                .add(overtimeAmount)
                .add(bonuses != null ? bonuses : BigDecimal.ZERO);

        BigDecimal isssCap = settings != null ? settings.getIsssSalaryCap() : new BigDecimal("1000.00");
        BigDecimal isssBase = grossSalary.min(isssCap);
        BigDecimal isssEmployeeRate = settings != null ? settings.getIsssEmployeeRate() : new BigDecimal("0.0300");
        BigDecimal isssEmployerRate = settings != null ? settings.getIsssEmployerRate() : new BigDecimal("0.0750");
        BigDecimal afpEmployeeRate  = settings != null ? settings.getAfpEmployeeRate()  : new BigDecimal("0.0725");
        BigDecimal afpEmployerRate  = settings != null ? settings.getAfpEmployerRate()  : new BigDecimal("0.0875");

        BigDecimal isssEmployee = isssBase.multiply(isssEmployeeRate).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal isssEmployer = isssBase.multiply(isssEmployerRate).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal afpEmployee  = grossSalary.multiply(afpEmployeeRate).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal afpEmployer  = grossSalary.multiply(afpEmployerRate).setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal isr = calculateIsr(grossSalary.subtract(isssEmployee).subtract(afpEmployee));

        BigDecimal totalDeductions = isssEmployee.add(afpEmployee).add(isr);
        BigDecimal netSalary = grossSalary.subtract(totalDeductions);
        BigDecimal totalEmployerCost = grossSalary.add(isssEmployer).add(afpEmployer);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("salarioBase", salary);
        details.put("horasExtra", overtimeHours);
        details.put("montoHorasExtra", overtimeAmount);
        details.put("bonificaciones", bonuses);
        details.put("salarioBruto", grossSalary);
        details.put("isssEmpleado", isssEmployee);
        details.put("afpEmpleado", afpEmployee);
        details.put("isr", isr);
        details.put("isssPatronal", isssEmployer);
        details.put("afpPatronal", afpEmployer);

        return PayrollLine.builder()
                .employee(employee)
                .salary(salary)
                .overtimeHours(overtimeHours != null ? overtimeHours : BigDecimal.ZERO)
                .overtimeAmount(overtimeAmount)
                .bonuses(bonuses != null ? bonuses : BigDecimal.ZERO)
                .grossSalary(grossSalary)
                .isssEmployee(isssEmployee)
                .afpEmployee(afpEmployee)
                .isr(isr)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .isssEmployer(isssEmployer)
                .afpEmployer(afpEmployer)
                .totalEmployerCost(totalEmployerCost)
                .conceptDetails(details)
                .build();
    }

    /**
     * ISR mensual El Salvador — tramos vigentes Art. 37 Ley de Renta.
     * Base = salario bruto - ISSS empleado - AFP empleado.
     */
    private BigDecimal calculateIsr(BigDecimal rentableIncome) {
        if (rentableIncome.compareTo(new BigDecimal("472.00")) <= 0) {
            return BigDecimal.ZERO;
        }
        if (rentableIncome.compareTo(new BigDecimal("895.24")) <= 0) {
            return rentableIncome.subtract(new BigDecimal("472.00"))
                    .multiply(new BigDecimal("0.10"))
                    .setScale(SCALE, RoundingMode.HALF_UP);
        }
        if (rentableIncome.compareTo(new BigDecimal("2038.10")) <= 0) {
            return rentableIncome.subtract(new BigDecimal("895.24"))
                    .multiply(new BigDecimal("0.20"))
                    .add(new BigDecimal("42.32"))
                    .setScale(SCALE, RoundingMode.HALF_UP);
        }
        return rentableIncome.subtract(new BigDecimal("2038.10"))
                .multiply(new BigDecimal("0.30"))
                .add(new BigDecimal("271.04"))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }
}
