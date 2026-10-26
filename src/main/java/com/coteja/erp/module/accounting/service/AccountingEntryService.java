package com.coteja.erp.module.accounting.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.accounting.dto.JournalEntryRequest;
import com.coteja.erp.module.accounting.entity.AccountingPackage;
import com.coteja.erp.module.accounting.repository.AccountingPackageRepository;
import com.coteja.erp.module.accounting.repository.JournalEntryRepository;
import com.coteja.erp.module.billing.entity.BillingInvoice;
import com.coteja.erp.module.inventory.entity.InventoryMovement;
import com.coteja.erp.module.payroll.entity.PayrollPeriod;
import com.coteja.erp.module.payroll.entity.PayrollSettings;
import com.coteja.erp.module.payroll.repository.PayrollSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Auto-generates journal entries from source documents.
 * Each method is idempotent — skips if an entry already exists for the source.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountingEntryService {

    private final JournalEntryService journalEntryService;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountingPackageRepository packageRepository;
    private final PayrollSettingsRepository payrollSettingsRepository;

    // ── FA: Facturación ───────────────────────────────────────────────────────

    public void generateFromBilling(BillingInvoice invoice) {
        Long companyId = invoice.getCompany().getId();

        if (journalEntryRepository.findBySourceTypeAndSourceId("FA", invoice.getId()).isPresent()) {
            log.debug("Journal entry FA already exists for BillingInvoice {}", invoice.getId());
            return;
        }

        Optional<AccountingPackage> pkgOpt = packageRepository.findByCompanyIdAndCode(companyId, "FA");
        if (pkgOpt.isEmpty()) {
            log.warn("No accounting package FA for company {}", companyId);
            return;
        }

        AccountingPackage pkg = pkgOpt.get();
        if (pkg.getDebitAccount() == null || pkg.getCreditAccount() == null) {
            log.warn("AccountingPackage FA for company {} missing accounts", companyId);
            return;
        }

        // DÉBITO: cuentas por cobrar (debitAccount) = total
        // CRÉDITO: ventas gravadas (creditAccount) = subtotal
        // CRÉDITO: IVA débito fiscal (secondaryCreditAccount) = iva
        List<JournalEntryRequest.LineRequest> lines = new ArrayList<>();
        lines.add(new JournalEntryRequest.LineRequest(
                pkg.getDebitAccount().getId(),
                pkg.getCostCenter() != null ? pkg.getCostCenter().getId() : null,
                "CxC: " + invoice.getCustomerName(),
                invoice.getTotal(),
                BigDecimal.ZERO,
                1
        ));
        lines.add(new JournalEntryRequest.LineRequest(
                pkg.getCreditAccount().getId(),
                null,
                "Venta: " + invoice.getNumberControl(),
                BigDecimal.ZERO,
                invoice.getSubtotal(),
                2
        ));
        if (invoice.getIva().compareTo(BigDecimal.ZERO) > 0 && pkg.getSecondaryCreditAccount() != null) {
            lines.add(new JournalEntryRequest.LineRequest(
                    pkg.getSecondaryCreditAccount().getId(),
                    null,
                    "IVA débito fiscal",
                    BigDecimal.ZERO,
                    invoice.getIva(),
                    3
            ));
        }

        emit("FA", invoice.getId(), invoice.getNumberControl(), pkg.getId(),
                "Factura " + invoice.getDocumentType() + " " + invoice.getNumberControl(),
                invoice.getIssuedAt().atStartOfDay().toLocalDate(), lines);
    }

    // ── CP: Compras ───────────────────────────────────────────────────────────

    public void generateFromPurchaseInvoice(
            com.coteja.erp.module.purchases.entity.PurchaseInvoice invoice) {
        Long companyId = invoice.getCompany().getId();

        if (journalEntryRepository.findBySourceTypeAndSourceId("CP", invoice.getId()).isPresent()) {
            log.debug("Journal entry CP already exists for PurchaseInvoice {}", invoice.getId());
            return;
        }

        Optional<AccountingPackage> pkgOpt = packageRepository.findByCompanyIdAndCode(companyId, "CP");
        if (pkgOpt.isEmpty()) {
            log.warn("No accounting package CP for company {}", companyId);
            return;
        }

        AccountingPackage pkg = pkgOpt.get();
        if (pkg.getDebitAccount() == null || pkg.getCreditAccount() == null) {
            log.warn("AccountingPackage CP for company {} missing accounts", companyId);
            return;
        }

        List<JournalEntryRequest.LineRequest> lines = new ArrayList<>();
        lines.add(new JournalEntryRequest.LineRequest(
                pkg.getDebitAccount().getId(),
                pkg.getCostCenter() != null ? pkg.getCostCenter().getId() : null,
                "Compra: " + invoice.getInvoiceNumber(),
                invoice.getSubtotal(),
                BigDecimal.ZERO,
                1
        ));
        if (invoice.getIva() != null && invoice.getIva().compareTo(BigDecimal.ZERO) > 0
                && pkg.getSecondaryDebitAccount() != null) {
            lines.add(new JournalEntryRequest.LineRequest(
                    pkg.getSecondaryDebitAccount().getId(),
                    null,
                    "IVA crédito fiscal",
                    invoice.getIva(),
                    BigDecimal.ZERO,
                    2
            ));
        }
        lines.add(new JournalEntryRequest.LineRequest(
                pkg.getCreditAccount().getId(),
                null,
                "Proveedor: " + (invoice.getSupplier() != null ? invoice.getSupplier().getName() : ""),
                BigDecimal.ZERO,
                invoice.getTotal(),
                3
        ));

        emit("CP", invoice.getId(), invoice.getInvoiceNumber(), pkg.getId(),
                "Compra " + invoice.getDocumentType() + " " + invoice.getInvoiceNumber(),
                invoice.getPurchaseDate(), lines);
    }

    // ── IN: Inventario ────────────────────────────────────────────────────────

    public void generateFromInventory(InventoryMovement movement) {
        Long companyId = movement.getCompany().getId();

        if (journalEntryRepository.findBySourceTypeAndSourceId("IN", movement.getId()).isPresent()) {
            log.debug("Journal entry IN already exists for InventoryMovement {}", movement.getId());
            return;
        }

        Optional<AccountingPackage> pkgOpt = packageRepository.findByCompanyIdAndCode(companyId, "IN");
        if (pkgOpt.isEmpty()) {
            log.warn("No accounting package IN for company {}", companyId);
            return;
        }

        AccountingPackage pkg = pkgOpt.get();
        if (pkg.getDebitAccount() == null || pkg.getCreditAccount() == null) {
            log.warn("AccountingPackage IN for company {} missing accounts", companyId);
            return;
        }

        // entry  → DÉBITO inventario, CRÉDITO costo de ventas
        // exit   → DÉBITO costo de ventas, CRÉDITO inventario
        boolean isEntry = InventoryMovement.MovementType.entry.equals(movement.getType());
        Long debitId  = isEntry ? pkg.getDebitAccount().getId()  : pkg.getCreditAccount().getId();
        Long creditId = isEntry ? pkg.getCreditAccount().getId() : pkg.getDebitAccount().getId();

        BigDecimal amount = movement.getQuantity(); // valor en unidades; sin precio unitario en el movimiento
        List<JournalEntryRequest.LineRequest> lines = List.of(
                new JournalEntryRequest.LineRequest(debitId, null,
                        (isEntry ? "Entrada" : "Salida") + " inventario",
                        amount, BigDecimal.ZERO, 1),
                new JournalEntryRequest.LineRequest(creditId, null,
                        (isEntry ? "Costo ventas" : "Inventario"),
                        BigDecimal.ZERO, amount, 2)
        );

        java.time.LocalDate date = movement.getCreatedAt() != null
                ? movement.getCreatedAt().toLocalDate()
                : java.time.LocalDate.now();

        emit("IN", movement.getId(), movement.getDocumentNumber(), pkg.getId(),
                (isEntry ? "Entrada" : "Salida") + " de inventario #" + movement.getId(),
                date, lines);
    }

    // ── NOM: Nómina ───────────────────────────────────────────────────────────

    public void generateFromPayroll(PayrollPeriod period) {
        Long companyId = period.getCompany().getId();

        if (journalEntryRepository.findBySourceTypeAndSourceId("NOM", period.getId()).isPresent()) {
            log.debug("Journal entry NOM already exists for PayrollPeriod {}", period.getId());
            return;
        }

        Optional<AccountingPackage> pkgOpt = packageRepository.findByCompanyIdAndCode(companyId, "NOM");
        if (pkgOpt.isEmpty()) {
            log.warn("No accounting package NOM for company {}", companyId);
            return;
        }

        AccountingPackage pkg = pkgOpt.get();
        PayrollSettings cfg = payrollSettingsRepository.findByCompanyId(companyId).orElse(null);

        if (cfg == null || cfg.getAccountSalaries() == null || cfg.getAccountSalariesPayable() == null) {
            log.warn("PayrollSettings for company {} not fully configured", companyId);
            return;
        }

        // Totales del período
        BigDecimal totalGross   = period.getLines().stream().map(l -> l.getGrossSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIsssEmp = period.getLines().stream().map(l -> l.getIsssEmployer())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAfpEmp  = period.getLines().stream().map(l -> l.getAfpEmployer())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIsss    = period.getLines().stream().map(l -> l.getIsssEmployee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAfp     = period.getLines().stream().map(l -> l.getAfpEmployee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIsr     = period.getLines().stream().map(l -> l.getIsr())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet     = period.getLines().stream().map(l -> l.getNetSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<JournalEntryRequest.LineRequest> lines = new ArrayList<>();
        int ord = 1;

        // DÉBITOS
        lines.add(line(cfg.getAccountSalaries().getId(),      "Gasto sueldos y salarios", totalGross, BigDecimal.ZERO, ord++));
        if (cfg.getAccountIsssEmployer() != null && totalIsssEmp.compareTo(BigDecimal.ZERO) > 0)
            lines.add(line(cfg.getAccountIsssEmployer().getId(), "Gasto ISSS patronal", totalIsssEmp, BigDecimal.ZERO, ord++));
        if (cfg.getAccountAfpEmployer() != null && totalAfpEmp.compareTo(BigDecimal.ZERO) > 0)
            lines.add(line(cfg.getAccountAfpEmployer().getId(),  "Gasto AFP patronal",  totalAfpEmp,  BigDecimal.ZERO, ord++));

        // CRÉDITOS
        lines.add(line(cfg.getAccountSalariesPayable().getId(), "Sueldos por pagar",    BigDecimal.ZERO, totalNet,   ord++));
        if (cfg.getAccountIsssPayable() != null && (totalIsss.add(totalIsssEmp)).compareTo(BigDecimal.ZERO) > 0)
            lines.add(line(cfg.getAccountIsssPayable().getId(), "ISSS por pagar",        BigDecimal.ZERO, totalIsss.add(totalIsssEmp), ord++));
        if (cfg.getAccountAfpPayable() != null && (totalAfp.add(totalAfpEmp)).compareTo(BigDecimal.ZERO) > 0)
            lines.add(line(cfg.getAccountAfpPayable().getId(),  "AFP por pagar",         BigDecimal.ZERO, totalAfp.add(totalAfpEmp),   ord++));
        if (cfg.getAccountIsrPayable() != null && totalIsr.compareTo(BigDecimal.ZERO) > 0)
            lines.add(line(cfg.getAccountIsrPayable().getId(),  "ISR por pagar",         BigDecimal.ZERO, totalIsr, ord));

        emit("NOM", period.getId(), period.getName(), pkg.getId(),
                "Nómina " + period.getName(),
                period.getPeriodEnd(), lines);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private JournalEntryRequest.LineRequest line(Long accountId, String desc,
                                                  BigDecimal debit, BigDecimal credit, int order) {
        return new JournalEntryRequest.LineRequest(accountId, null, desc, debit, credit, order);
    }

    private void emit(String sourceType, Long sourceId, String reference,
                      Long packageId, String description,
                      java.time.LocalDate date,
                      List<JournalEntryRequest.LineRequest> lines) {
        JournalEntryRequest request = new JournalEntryRequest(
                date, description, reference, packageId, null, lines);
        try {
            journalEntryService.create(request);
        } catch (Exception e) {
            log.error("Failed to auto-generate journal entry {} {} {}: {}",
                    sourceType, sourceId, reference, e.getMessage());
        }
    }
}
