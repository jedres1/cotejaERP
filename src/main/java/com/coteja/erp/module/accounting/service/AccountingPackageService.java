package com.coteja.erp.module.accounting.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.accounting.dto.PackageRequest;
import com.coteja.erp.module.accounting.dto.PackageResponse;
import com.coteja.erp.module.accounting.entity.AccountingAccount;
import com.coteja.erp.module.accounting.entity.AccountingPackage;
import com.coteja.erp.module.accounting.entity.CostCenter;
import com.coteja.erp.module.accounting.repository.AccountingAccountRepository;
import com.coteja.erp.module.accounting.repository.AccountingPackageRepository;
import com.coteja.erp.module.accounting.repository.CostCenterRepository;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountingPackageService {

    private final AccountingPackageRepository packageRepository;
    private final AccountingAccountRepository accountRepository;
    private final CostCenterRepository costCenterRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<PackageResponse> findAll() {
        Long companyId = TenantContext.getCompanyId();
        return packageRepository.findByCompanyId(companyId)
                .stream().map(PackageResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PackageResponse findById(Long id) {
        return packageRepository.findById(id)
                .map(PackageResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingPackage", id));
    }

    public PackageResponse create(PackageRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        AccountingPackage pkg = AccountingPackage.builder()
                .customer(company.getCustomer())
                .company(company)
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .type(request.type() != null ? request.type() : AccountingPackage.PackageType.automatico)
                .debitAccount(resolveAccount(request.debitAccountId()))
                .creditAccount(resolveAccount(request.creditAccountId()))
                .secondaryDebitAccount(resolveAccount(request.secondaryDebitAccountId()))
                .secondaryCreditAccount(resolveAccount(request.secondaryCreditAccountId()))
                .costCenter(resolveCostCenter(request.costCenterId()))
                .build();

        return PackageResponse.from(packageRepository.save(pkg));
    }

    public PackageResponse update(Long id, PackageRequest request) {
        AccountingPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingPackage", id));
        pkg.setName(request.name());
        pkg.setDescription(request.description());
        pkg.setDebitAccount(resolveAccount(request.debitAccountId()));
        pkg.setCreditAccount(resolveAccount(request.creditAccountId()));
        pkg.setSecondaryDebitAccount(resolveAccount(request.secondaryDebitAccountId()));
        pkg.setSecondaryDebitAccount(resolveAccount(request.secondaryDebitAccountId()));
        pkg.setCostCenter(resolveCostCenter(request.costCenterId()));
        if (request.isActive() != null) pkg.setActive(request.isActive());
        return PackageResponse.from(packageRepository.save(pkg));
    }

    /** Atomically reserves the next correlative number using PESSIMISTIC_WRITE lock. */
    public String reserveNextNumber(Long packageId) {
        AccountingPackage pkg = packageRepository.findByIdForUpdate(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingPackage", packageId));
        pkg.setLastCorrelative(pkg.getLastCorrelative() + 1);
        String number = String.format("%s-%04d", pkg.getCode(), pkg.getLastCorrelative());
        packageRepository.save(pkg);
        return number;
    }

    private AccountingAccount resolveAccount(Long id) {
        if (id == null) return null;
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingAccount", id));
    }

    private CostCenter resolveCostCenter(Long id) {
        if (id == null) return null;
        return costCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostCenter", id));
    }
}
