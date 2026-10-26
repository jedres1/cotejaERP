package com.coteja.erp.module.accounting.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.accounting.dto.PeriodResponse;
import com.coteja.erp.module.accounting.entity.AccountingPeriod;
import com.coteja.erp.module.accounting.entity.JournalEntry;
import com.coteja.erp.module.accounting.repository.AccountingPeriodRepository;
import com.coteja.erp.module.accounting.repository.JournalEntryRepository;
import com.coteja.erp.module.admin.entity.User;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountingPeriodService {

    private final AccountingPeriodRepository periodRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PeriodResponse> findAll() {
        Long companyId = TenantContext.getCompanyId();
        return periodRepository.findByCompanyIdOrderByYearDescMonthDesc(companyId)
                .stream().map(PeriodResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PeriodResponse findById(Long id) {
        return periodRepository.findById(id)
                .map(PeriodResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingPeriod", id));
    }

    public PeriodResponse openPeriod(Integer year, Integer month) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        periodRepository.findByCompanyIdAndYearAndMonth(companyId, year, month)
                .ifPresent(p -> { throw new IllegalStateException("El período ya existe"); });

        AccountingPeriod period = AccountingPeriod.builder()
                .company(company)
                .year(year)
                .month(month)
                .name(AccountingPeriod.labelFor(year, month))
                .status("abierto")
                .openedAt(LocalDateTime.now())
                .build();

        return PeriodResponse.from(periodRepository.save(period));
    }

    public PeriodResponse closePeriod(Long id) {
        Long companyId = TenantContext.getCompanyId();
        AccountingPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingPeriod", id));

        if (!period.isOpen()) {
            throw new IllegalStateException("El período ya está cerrado");
        }

        List<JournalEntry> draftEntries = journalEntryRepository
                .findByPeriod(companyId, period.getStartDate(), period.getEndDate())
                .stream()
                .filter(e -> JournalEntry.EntryStatus.borrador.equals(e.getStatus()))
                .toList();

        if (!draftEntries.isEmpty()) {
            throw new IllegalStateException(
                    "Existen " + draftEntries.size() + " partidas en borrador. Apruebe o anule antes de cerrar el período.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByEmail(username).ifPresent(period::setClosedBy);
        period.setStatus("cerrado");
        period.setClosedAt(LocalDateTime.now());

        return PeriodResponse.from(periodRepository.save(period));
    }

    public PeriodResponse reopenPeriod(Long id) {
        AccountingPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingPeriod", id));
        period.setStatus("abierto");
        period.setClosedAt(null);
        period.setClosedBy(null);
        return PeriodResponse.from(periodRepository.save(period));
    }
}
