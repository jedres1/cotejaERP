package com.coteja.erp.module.banking.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.banking.dto.*;
import com.coteja.erp.module.banking.entity.*;
import com.coteja.erp.module.banking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BankingService {

    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final BankReconciliationRepository reconciliationRepository;
    private final CompanyRepository companyRepository;

    // ---- Bank Accounts ----

    @Transactional(readOnly = true)
    public List<BankAccountResponse> findAllAccounts() {
        return accountRepository.findByCompanyId(TenantContext.getCompanyId())
                .stream().map(BankAccountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BankAccountResponse findAccountById(Long id) {
        return accountRepository.findById(id)
                .map(BankAccountResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", id));
    }

    public BankAccountResponse createAccount(BankAccountRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        BankAccount account = BankAccount.builder()
                .customer(company.getCustomer())
                .company(company)
                .name(request.name())
                .bankName(request.bankName())
                .accountNumber(request.accountNumber())
                .accountType(request.accountType())
                .currency(request.currency() != null ? request.currency() : "USD")
                .openingBalance(request.openingBalance() != null ? request.openingBalance() : java.math.BigDecimal.ZERO)
                .active(request.isActive() == null || request.isActive())
                .notes(request.notes())
                .build();
        return BankAccountResponse.from(accountRepository.save(account));
    }

    public BankAccountResponse updateAccount(Long id, BankAccountRequest request) {
        BankAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", id));
        account.setName(request.name());
        account.setBankName(request.bankName());
        account.setAccountNumber(request.accountNumber());
        account.setAccountType(request.accountType());
        if (request.currency() != null) account.setCurrency(request.currency());
        if (request.openingBalance() != null) account.setOpeningBalance(request.openingBalance());
        if (request.isActive() != null) account.setActive(request.isActive());
        account.setNotes(request.notes());
        return BankAccountResponse.from(accountRepository.save(account));
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    // ---- Transactions ----

    @Transactional(readOnly = true)
    public PageResponse<BankTransactionResponse> findTransactions(Long accountId, Pageable pageable) {
        return PageResponse.from(
                transactionRepository.findByBankAccountId(accountId, pageable)
                        .map(BankTransactionResponse::from)
        );
    }

    public BankTransactionResponse createTransaction(BankTransactionRequest request) {
        BankAccount account = accountRepository.findById(request.bankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", request.bankAccountId()));
        BankTransaction tx = BankTransaction.builder()
                .bankAccount(account)
                .type(request.type())
                .transactionDate(request.transactionDate())
                .amount(request.amount())
                .reference(request.reference())
                .notes(request.notes())
                .build();
        return BankTransactionResponse.from(transactionRepository.save(tx));
    }

    // ---- Reconciliations ----

    @Transactional(readOnly = true)
    public PageResponse<BankReconciliationResponse> findReconciliations(Long accountId, Pageable pageable) {
        return PageResponse.from(
                reconciliationRepository.findByBankAccountId(accountId, pageable)
                        .map(BankReconciliationResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public BankReconciliationResponse findReconciliationById(Long id) {
        return reconciliationRepository.findById(id)
                .map(BankReconciliationResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("BankReconciliation", id));
    }

    public BankReconciliationResponse createReconciliation(BankReconciliationRequest request) {
        BankAccount account = accountRepository.findById(request.bankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", request.bankAccountId()));
        BankReconciliation rec = BankReconciliation.builder()
                .bankAccount(account)
                .periodYear(request.periodYear())
                .periodMonth(request.periodMonth())
                .statementBalance(request.statementBalance())
                .notes(request.notes())
                .build();
        return BankReconciliationResponse.from(reconciliationRepository.save(rec));
    }

    public BankReconciliationResponse completeReconciliation(Long id) {
        BankReconciliation rec = reconciliationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankReconciliation", id));
        rec.setStatus("completed");
        rec.setCompletedAt(LocalDateTime.now());
        // Marcar todas las transacciones de esta conciliación como reconciliadas
        transactionRepository.findByBankAccountId(rec.getBankAccount().getId(),
                org.springframework.data.domain.Pageable.unpaged())
                .forEach(t -> {
                    if (!t.isReconciled()) {
                        t.setReconciled(true);
                        t.setReconciliation(rec);
                        transactionRepository.save(t);
                    }
                });
        return BankReconciliationResponse.from(reconciliationRepository.save(rec));
    }

    public void toggleReconcileTransaction(Long transactionId) {
        BankTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("BankTransaction", transactionId));
        tx.setReconciled(!tx.isReconciled());
        transactionRepository.save(tx);
    }
}
