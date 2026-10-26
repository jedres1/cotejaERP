package com.coteja.erp.module.accounting.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.accounting.dto.JournalEntryRequest;
import com.coteja.erp.module.accounting.dto.JournalEntryResponse;
import com.coteja.erp.module.accounting.entity.*;
import com.coteja.erp.module.accounting.repository.*;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository entryRepository;
    private final JournalEntryLineRepository lineRepository;
    private final AccountingPackageService packageService;
    private final AccountingPackageRepository packageRepository;
    private final AccountingAccountRepository accountRepository;
    private final CostCenterRepository costCenterRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<JournalEntryResponse> findAll(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return PageResponse.from(
                entryRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                        .map(JournalEntryResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public JournalEntryResponse findById(Long id) {
        return entryRepository.findById(id)
                .map(JournalEntryResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));
    }

    public JournalEntryResponse create(JournalEntryRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        AccountingPackage pkg = null;
        String entryNumber;
        if (request.accountingPackageId() != null) {
            entryNumber = packageService.reserveNextNumber(request.accountingPackageId());
            pkg = packageRepository.findById(request.accountingPackageId()).orElse(null);
        } else {
            entryNumber = "CG-" + System.currentTimeMillis();
        }

        JournalEntry entry = JournalEntry.builder()
                .customer(company.getCustomer())
                .company(company)
                .entryNumber(entryNumber)
                .entryDate(request.entryDate())
                .description(request.description())
                .reference(request.reference())
                .accountingPackage(pkg)
                .notes(request.notes())
                .build();

        buildLines(entry, request.lines());
        validateBalance(entry);

        return JournalEntryResponse.from(entryRepository.save(entry));
    }

    public JournalEntryResponse update(Long id, JournalEntryRequest request) {
        JournalEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (!entry.isEditable()) {
            throw new IllegalStateException("Solo se pueden editar partidas en estado borrador");
        }

        entry.setEntryDate(request.entryDate());
        entry.setDescription(request.description());
        entry.setReference(request.reference());
        entry.setNotes(request.notes());
        entry.getLines().clear();
        buildLines(entry, request.lines());
        validateBalance(entry);

        return JournalEntryResponse.from(entryRepository.save(entry));
    }

    public void approve(Long id) {
        JournalEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (!entry.isEditable()) {
            throw new IllegalStateException("La partida no está en estado borrador");
        }
        if (!entry.isBalanced()) {
            throw new IllegalStateException("La partida no está balanceada (débito ≠ crédito)");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByEmail(username).ifPresent(entry::setApprovedBy);
        entry.setApprovedAt(LocalDateTime.now());
        entry.setStatus(JournalEntry.EntryStatus.aprobado);
        entryRepository.save(entry);
    }

    public void annul(Long id) {
        JournalEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (JournalEntry.EntryStatus.anulado.equals(entry.getStatus())) {
            throw new IllegalStateException("La partida ya está anulada");
        }

        entry.setStatus(JournalEntry.EntryStatus.anulado);
        entryRepository.save(entry);
    }

    public void softDelete(Long id) {
        JournalEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));
        if (!entry.isEditable()) {
            throw new IllegalStateException("Solo se pueden eliminar partidas en borrador");
        }
        entry.setDeletedAt(LocalDateTime.now());
        entryRepository.save(entry);
    }

    private void buildLines(JournalEntry entry, List<JournalEntryRequest.LineRequest> lineRequests) {
        List<JournalEntryLine> lines = new ArrayList<>();
        int order = 1;
        for (JournalEntryRequest.LineRequest lr : lineRequests) {
            var account = accountRepository.findById(lr.accountId())
                    .orElseThrow(() -> new ResourceNotFoundException("AccountingAccount", lr.accountId()));
            CostCenter cc = null;
            if (lr.costCenterId() != null) {
                cc = costCenterRepository.findById(lr.costCenterId())
                        .orElseThrow(() -> new ResourceNotFoundException("CostCenter", lr.costCenterId()));
            }
            JournalEntryLine line = JournalEntryLine.builder()
                    .journalEntry(entry)
                    .account(account)
                    .costCenter(cc)
                    .description(lr.description())
                    .debit(lr.debit())
                    .credit(lr.credit())
                    .sortOrder(lr.sortOrder() != null ? lr.sortOrder() : order)
                    .build();
            lines.add(line);
            order++;
        }
        entry.getLines().addAll(lines);
    }

    private void validateBalance(JournalEntry entry) {
        if (!entry.getLines().isEmpty() && !entry.isBalanced()) {
            throw new IllegalStateException("La partida no está balanceada: débito="
                    + entry.getTotalDebit() + " crédito=" + entry.getTotalCredit());
        }
    }
}
