package com.coteja.erp.module.accounting.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.accounting.dto.AccountRequest;
import com.coteja.erp.module.accounting.dto.AccountResponse;
import com.coteja.erp.module.accounting.entity.AccountingAccount;
import com.coteja.erp.module.accounting.repository.AccountingAccountRepository;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountingService {

    private final AccountingAccountRepository accountRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        Long companyId = TenantContext.getCompanyId();
        List<AccountingAccount> accounts = accountRepository.findByCompanyIdOrderByCode(companyId);
        return accounts.stream()
                .filter(a -> a.getParent() == null)
                .map(AccountResponse::fromWithChildren)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findLeafAccounts() {
        Long companyId = TenantContext.getCompanyId();
        return accountRepository.findLeafAccounts(companyId)
                .stream().map(AccountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(Long id) {
        return accountRepository.findById(id)
                .map(AccountResponse::fromWithChildren)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingAccount", id));
    }

    public AccountResponse create(AccountRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        AccountingAccount parent = null;
        int level = 1;
        if (request.parentId() != null) {
            parent = accountRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("AccountingAccount", request.parentId()));
            level = parent.getLevel() + 1;
        }

        AccountingAccount.AccountType type = request.type() != null
                ? request.type()
                : AccountingAccount.typeFromCode(request.code());
        AccountingAccount.AccountNature nature = request.nature() != null
                ? request.nature()
                : AccountingAccount.natureFromType(type);

        AccountingAccount account = AccountingAccount.builder()
                .customer(company.getCustomer())
                .company(company)
                .code(request.code())
                .name(request.name())
                .type(type)
                .nature(nature)
                .parent(parent)
                .level(level)
                .notes(request.notes())
                .build();

        return AccountResponse.from(accountRepository.save(account));
    }

    public AccountResponse update(Long id, AccountRequest request) {
        AccountingAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingAccount", id));

        account.setCode(request.code());
        account.setName(request.name());
        account.setNotes(request.notes());
        if (request.isActive() != null) {
            account.setActive(request.isActive());
        }
        return AccountResponse.from(accountRepository.save(account));
    }

    public void delete(Long id) {
        AccountingAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingAccount", id));
        if (!account.isLeaf()) {
            throw new IllegalStateException("No se puede eliminar una cuenta que tiene subcuentas");
        }
        accountRepository.deleteById(id);
    }
}
