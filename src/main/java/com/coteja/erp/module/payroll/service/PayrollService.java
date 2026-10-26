package com.coteja.erp.module.payroll.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.accounting.entity.AccountingAccount;
import com.coteja.erp.module.accounting.entity.AccountingPackage;
import com.coteja.erp.module.accounting.repository.AccountingAccountRepository;
import com.coteja.erp.module.accounting.repository.AccountingPackageRepository;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.admin.repository.UserRepository;
import com.coteja.erp.module.payroll.dto.*;
import com.coteja.erp.module.payroll.entity.*;
import com.coteja.erp.module.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {

    private final EmployeeRepository employeeRepository;
    private final PayrollConceptRepository conceptRepository;
    private final PayrollPeriodRepository periodRepository;
    private final PayrollSettingsRepository settingsRepository;
    private final PayrollCalculatorService calculator;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final AccountingAccountRepository accountRepository;
    private final AccountingPackageRepository accountingPackageRepository;

    // ── Empleados ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> findEmployees(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return PageResponse.from(
                employeeRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                        .map(EmployeeResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        Employee emp = Employee.builder()
                .customer(company.getCustomer())
                .company(company)
                .code(request.code())
                .name(request.name())
                .email(request.email())
                .dui(request.dui())
                .isssNumber(request.isssNumber())
                .nup(request.nup())
                .afp(request.afp())
                .position(request.position())
                .department(request.department())
                .hireDate(request.hireDate())
                .salary(request.salary())
                .bankName(request.bankName())
                .bankAccount(request.bankAccount())
                .active(request.isActive() == null || request.isActive())
                .build();

        return EmployeeResponse.from(employeeRepository.save(emp));
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        emp.setName(request.name());
        emp.setEmail(request.email());
        emp.setDui(request.dui());
        emp.setIsssNumber(request.isssNumber());
        emp.setNup(request.nup());
        emp.setAfp(request.afp());
        emp.setPosition(request.position());
        emp.setDepartment(request.department());
        emp.setSalary(request.salary());
        emp.setBankName(request.bankName());
        emp.setBankAccount(request.bankAccount());
        if (request.isActive() != null) emp.setActive(request.isActive());
        return EmployeeResponse.from(employeeRepository.save(emp));
    }

    public void softDeleteEmployee(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        emp.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(emp);
    }

    // ── Conceptos de nómina ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PayrollConceptResponse> findConcepts() {
        Long companyId = TenantContext.getCompanyId();
        return conceptRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream().map(PayrollConceptResponse::from).toList();
    }

    public PayrollConceptResponse createConcept(PayrollConceptRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        PayrollConcept concept = PayrollConcept.builder()
                .customer(company.getCustomer())
                .company(company)
                .name(request.name())
                .type(request.type())
                .calculationMethod(request.calculationMethod())
                .amount(request.amount() != null ? request.amount() : BigDecimal.ZERO)
                .rate(request.rate() != null ? request.rate() : BigDecimal.ZERO)
                .formula(request.formula())
                .systemKey(request.systemKey())
                .appliesToAll(request.appliesToAll() == null || request.appliesToAll())
                .active(request.isActive() == null || request.isActive())
                .build();

        return PayrollConceptResponse.from(conceptRepository.save(concept));
    }

    public PayrollConceptResponse updateConcept(Long id, PayrollConceptRequest request) {
        PayrollConcept concept = conceptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollConcept", id));
        concept.setName(request.name());
        concept.setType(request.type());
        concept.setCalculationMethod(request.calculationMethod());
        concept.setAmount(request.amount() != null ? request.amount() : BigDecimal.ZERO);
        concept.setRate(request.rate() != null ? request.rate() : BigDecimal.ZERO);
        concept.setFormula(request.formula());
        concept.setSystemKey(request.systemKey());
        if (request.appliesToAll() != null) concept.setAppliesToAll(request.appliesToAll());
        if (request.isActive() != null) concept.setActive(request.isActive());
        return PayrollConceptResponse.from(conceptRepository.save(concept));
    }

    public void deleteConcept(Long id) {
        PayrollConcept concept = conceptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollConcept", id));
        concept.setActive(false);
        conceptRepository.save(concept);
    }

    // ── Configuración de nómina ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PayrollSettingsResponse findSettings() {
        Long companyId = TenantContext.getCompanyId();
        return settingsRepository.findByCompanyId(companyId)
                .map(PayrollSettingsResponse::from)
                .orElse(null);
    }

    public PayrollSettingsResponse upsertSettings(PayrollSettingsRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        PayrollSettings settings = settingsRepository.findByCompanyId(companyId)
                .orElseGet(() -> PayrollSettings.builder().company(company).build());

        if (request.accountingPackageId() != null)
            settings.setAccountingPackage(accountingPackageRepository.findById(request.accountingPackageId()).orElse(null));
        settings.setAccountSalaries(resolveAccount(request.accountSalariesId()));
        settings.setAccountIsssEmployer(resolveAccount(request.accountIsssEmployerId()));
        settings.setAccountAfpEmployer(resolveAccount(request.accountAfpEmployerId()));
        settings.setAccountIsssPayable(resolveAccount(request.accountIsssPayableId()));
        settings.setAccountAfpPayable(resolveAccount(request.accountAfpPayableId()));
        settings.setAccountIsrPayable(resolveAccount(request.accountIsrPayableId()));
        settings.setAccountSalariesPayable(resolveAccount(request.accountSalariesPayableId()));
        if (request.isssSalaryCap() != null)      settings.setIsssSalaryCap(request.isssSalaryCap());
        if (request.isssEmployeeRate() != null)   settings.setIsssEmployeeRate(request.isssEmployeeRate());
        if (request.isssEmployerRate() != null)   settings.setIsssEmployerRate(request.isssEmployerRate());
        if (request.afpEmployeeRate() != null)    settings.setAfpEmployeeRate(request.afpEmployeeRate());
        if (request.afpEmployerRate() != null)    settings.setAfpEmployerRate(request.afpEmployerRate());

        return PayrollSettingsResponse.from(settingsRepository.save(settings));
    }

    private AccountingAccount resolveAccount(Long id) {
        if (id == null) return null;
        return accountRepository.findById(id).orElse(null);
    }

    // ── Períodos de nómina ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<PayrollPeriodResponse> findPeriods(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return PageResponse.from(
                periodRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                        .map(PayrollPeriodResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public PayrollPeriodResponse findPeriodById(Long id) {
        return periodRepository.findById(id)
                .map(PayrollPeriodResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollPeriod", id));
    }

    public PayrollPeriodResponse createPeriod(PayrollPeriodRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        PayrollPeriod period = PayrollPeriod.builder()
                .customer(company.getCustomer())
                .company(company)
                .name(request.name())
                .periodStart(request.periodStart())
                .periodEnd(request.periodEnd())
                .notes(request.notes())
                .build();

        PayrollSettings settings = settingsRepository.findByCompanyId(companyId).orElse(null);
        List<Employee> employees = employeeRepository.findByCompanyIdAndActiveTrueAndDeletedAtIsNull(companyId);

        for (Employee emp : employees) {
            PayrollLine line = calculator.calculate(emp, settings, BigDecimal.ZERO, BigDecimal.ZERO);
            line.setPayrollPeriod(period);
            period.getLines().add(line);
        }

        return PayrollPeriodResponse.from(periodRepository.save(period));
    }

    public PayrollPeriodResponse applyPeriod(Long id) {
        PayrollPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollPeriod", id));

        if (!period.isDraft()) {
            throw new IllegalStateException("El período ya fue aplicado");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByEmail(username).ifPresent(period::setAppliedBy);
        period.setStatus(PayrollPeriod.PeriodStatus.aplicado);
        period.setAppliedAt(LocalDateTime.now());

        return PayrollPeriodResponse.from(periodRepository.save(period));
    }
}
