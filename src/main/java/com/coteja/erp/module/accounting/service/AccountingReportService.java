package com.coteja.erp.module.accounting.service;

import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.accounting.entity.AccountingAccount;
import com.coteja.erp.module.accounting.entity.JournalEntry;
import com.coteja.erp.module.accounting.entity.JournalEntryLine;
import com.coteja.erp.module.accounting.repository.AccountingAccountRepository;
import com.coteja.erp.module.accounting.repository.JournalEntryLineRepository;
import com.coteja.erp.module.accounting.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountingReportService {

    private final AccountingAccountRepository accountRepository;
    private final JournalEntryLineRepository lineRepository;
    private final JournalEntryRepository entryRepository;

    public List<Map<String, Object>> trialBalance(LocalDate from, LocalDate to) {
        Long companyId = TenantContext.getCompanyId();
        List<AccountingAccount> accounts = accountRepository.findByCompanyIdAndActiveTrueOrderByCode(companyId);
        List<Map<String, Object>> rows = new ArrayList<>();

        for (AccountingAccount acc : accounts) {
            BigDecimal balance = lineRepository.calculateBalance(acc.getId(), companyId, to);
            if (balance.compareTo(BigDecimal.ZERO) == 0) continue;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("accountId", acc.getId());
            row.put("code", acc.getCode());
            row.put("name", acc.getName());
            row.put("type", acc.getType().name());
            row.put("nature", acc.getNature().name());

            boolean deudora = AccountingAccount.AccountNature.deudora.equals(acc.getNature());
            row.put("debit", deudora && balance.compareTo(BigDecimal.ZERO) > 0 ? balance : BigDecimal.ZERO);
            row.put("credit", !deudora && balance.compareTo(BigDecimal.ZERO) < 0 ? balance.abs() : BigDecimal.ZERO);
            row.put("balance", balance);
            rows.add(row);
        }
        return rows;
    }

    public Map<String, Object> balanceSheet(LocalDate asOf) {
        Long companyId = TenantContext.getCompanyId();
        List<AccountingAccount> accounts = accountRepository.findByCompanyIdAndActiveTrueOrderByCode(companyId);

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> assets = new ArrayList<>();
        List<Map<String, Object>> liabilities = new ArrayList<>();
        List<Map<String, Object>> equity = new ArrayList<>();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        for (AccountingAccount acc : accounts) {
            if (!acc.isLeaf()) continue;
            BigDecimal balance = lineRepository.calculateBalance(acc.getId(), companyId, asOf);
            if (balance.compareTo(BigDecimal.ZERO) == 0) continue;

            Map<String, Object> row = accountRow(acc, balance);
            switch (acc.getType()) {
                case activo -> { assets.add(row); totalAssets = totalAssets.add(balance); }
                case pasivo -> { liabilities.add(row); totalLiabilities = totalLiabilities.add(balance); }
                case patrimonio -> { equity.add(row); totalEquity = totalEquity.add(balance); }
                default -> {}
            }
        }

        result.put("assets", assets);
        result.put("totalAssets", totalAssets);
        result.put("liabilities", liabilities);
        result.put("totalLiabilities", totalLiabilities);
        result.put("equity", equity);
        result.put("totalEquity", totalEquity);
        result.put("totalLiabilitiesAndEquity", totalLiabilities.add(totalEquity));
        result.put("asOf", asOf);
        return result;
    }

    public Map<String, Object> incomeStatement(LocalDate from, LocalDate to) {
        Long companyId = TenantContext.getCompanyId();
        List<AccountingAccount> accounts = accountRepository.findByCompanyIdAndActiveTrueOrderByCode(companyId);

        List<Map<String, Object>> incomeRows = new ArrayList<>();
        List<Map<String, Object>> expenseRows = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (AccountingAccount acc : accounts) {
            if (!acc.isLeaf()) continue;
            BigDecimal balance = lineRepository.calculateBalance(acc.getId(), companyId, to);
            if (balance.compareTo(BigDecimal.ZERO) == 0) continue;

            Map<String, Object> row = accountRow(acc, balance);
            switch (acc.getType()) {
                case ingreso -> { incomeRows.add(row); totalIncome = totalIncome.add(balance.abs()); }
                case gasto  -> { expenseRows.add(row); totalExpenses = totalExpenses.add(balance.abs()); }
                default -> {}
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("income", incomeRows);
        result.put("totalIncome", totalIncome);
        result.put("expenses", expenseRows);
        result.put("totalExpenses", totalExpenses);
        result.put("netIncome", totalIncome.subtract(totalExpenses));
        result.put("from", from);
        result.put("to", to);
        return result;
    }

    public List<Map<String, Object>> ledger(Long accountId, LocalDate from, LocalDate to) {
        Long companyId = TenantContext.getCompanyId();
        List<JournalEntryLine> lines = lineRepository.findLedger(accountId, companyId, from, to);

        BigDecimal runningBalance = BigDecimal.ZERO;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (JournalEntryLine l : lines) {
            runningBalance = runningBalance.add(l.getDebit()).subtract(l.getCredit());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", l.getJournalEntry().getEntryDate());
            row.put("entryNumber", l.getJournalEntry().getEntryNumber());
            row.put("description", l.getDescription() != null ? l.getDescription() : l.getJournalEntry().getDescription());
            row.put("debit", l.getDebit());
            row.put("credit", l.getCredit());
            row.put("balance", runningBalance);
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> salesBook(LocalDate from, LocalDate to) {
        Long companyId = TenantContext.getCompanyId();
        return entryRepository.findByPeriod(companyId, from, to).stream()
                .filter(e -> "FA".equals(e.getAccountingPackage() != null ? e.getAccountingPackage().getCode() : null))
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", e.getEntryDate());
                    row.put("entryNumber", e.getEntryNumber());
                    row.put("description", e.getDescription());
                    row.put("reference", e.getReference());
                    row.put("totalDebit", e.getTotalDebit());
                    row.put("totalCredit", e.getTotalCredit());
                    return row;
                }).toList();
    }

    public List<Map<String, Object>> purchasesBook(LocalDate from, LocalDate to) {
        Long companyId = TenantContext.getCompanyId();
        return entryRepository.findByPeriod(companyId, from, to).stream()
                .filter(e -> "CP".equals(e.getAccountingPackage() != null ? e.getAccountingPackage().getCode() : null))
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", e.getEntryDate());
                    row.put("entryNumber", e.getEntryNumber());
                    row.put("description", e.getDescription());
                    row.put("reference", e.getReference());
                    row.put("totalDebit", e.getTotalDebit());
                    row.put("totalCredit", e.getTotalCredit());
                    return row;
                }).toList();
    }

    private Map<String, Object> accountRow(AccountingAccount acc, BigDecimal balance) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("accountId", acc.getId());
        row.put("code", acc.getCode());
        row.put("name", acc.getName());
        row.put("balance", balance);
        return row;
    }
}
