package com.coteja.erp.module.accounting.controller;

import com.coteja.erp.module.accounting.dto.*;
import com.coteja.erp.module.accounting.service.*;
import com.coteja.erp.module.admin.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/contabilidad")
@RequiredArgsConstructor
@PreAuthorize("@moduleAccess.hasAccess(authentication,'accounting')")
public class AccountingController {

    private final AccountingService accountingService;
    private final CostCenterService costCenterService;
    private final AccountingPackageService packageService;
    private final AccountingPeriodService periodService;
    private final AccountingReportService reportService;

    // ── Catálogo de cuentas ──────────────────────────────────────────────────

    @GetMapping("/catalogo")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getCatalog() {
        return ResponseEntity.ok(ApiResponse.ok(accountingService.findAll()));
    }

    @GetMapping("/catalogo/hojas")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getLeafAccounts() {
        return ResponseEntity.ok(ApiResponse.ok(accountingService.findLeafAccounts()));
    }

    @GetMapping("/catalogo/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(accountingService.findById(id)));
    }

    @PostMapping("/catalogo")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(accountingService.create(request)));
    }

    @PutMapping("/catalogo/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable Long id, @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(accountingService.update(id, request)));
    }

    @DeleteMapping("/catalogo/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        accountingService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Centros de costo ─────────────────────────────────────────────────────

    @GetMapping("/centros-costo")
    public ResponseEntity<ApiResponse<List<CostCenterResponse>>> getCostCenters() {
        return ResponseEntity.ok(ApiResponse.ok(costCenterService.findAll()));
    }

    @PostMapping("/centros-costo")
    public ResponseEntity<ApiResponse<CostCenterResponse>> createCostCenter(
            @Valid @RequestBody CostCenterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(costCenterService.create(request)));
    }

    @PutMapping("/centros-costo/{id}")
    public ResponseEntity<ApiResponse<CostCenterResponse>> updateCostCenter(
            @PathVariable Long id, @Valid @RequestBody CostCenterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(costCenterService.update(id, request)));
    }

    @DeleteMapping("/centros-costo/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCostCenter(@PathVariable Long id) {
        costCenterService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Paquetes contables ───────────────────────────────────────────────────

    @GetMapping("/paquetes")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getPackages() {
        return ResponseEntity.ok(ApiResponse.ok(packageService.findAll()));
    }

    @GetMapping("/paquetes/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackage(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(packageService.findById(id)));
    }

    @PostMapping("/paquetes")
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(@Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(packageService.create(request)));
    }

    @PutMapping("/paquetes/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(
            @PathVariable Long id, @Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(packageService.update(id, request)));
    }

    // ── Períodos contables ───────────────────────────────────────────────────

    @GetMapping("/periodos")
    public ResponseEntity<ApiResponse<List<PeriodResponse>>> getPeriods() {
        return ResponseEntity.ok(ApiResponse.ok(periodService.findAll()));
    }

    @PostMapping("/periodos/abrir")
    public ResponseEntity<ApiResponse<PeriodResponse>> openPeriod(@RequestBody Map<String, Integer> body) {
        Integer year = body.get("year");
        Integer month = body.get("month");
        return ResponseEntity.ok(ApiResponse.ok(periodService.openPeriod(year, month)));
    }

    @PostMapping("/periodos/{id}/cerrar")
    public ResponseEntity<ApiResponse<PeriodResponse>> closePeriod(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(periodService.closePeriod(id)));
    }

    @PostMapping("/periodos/{id}/reabrir")
    public ResponseEntity<ApiResponse<PeriodResponse>> reopenPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(periodService.reopenPeriod(id)));
    }

    // ── Reportes ─────────────────────────────────────────────────────────────

    @GetMapping("/balanza-comprobacion")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> trialBalance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.trialBalance(from, to)));
    }

    @GetMapping("/balance-general")
    public ResponseEntity<ApiResponse<Map<String, Object>>> balanceSheet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.balanceSheet(asOf)));
    }

    @GetMapping("/estado-resultados")
    public ResponseEntity<ApiResponse<Map<String, Object>>> incomeStatement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.incomeStatement(from, to)));
    }

    @GetMapping("/libro-mayor/{accountId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> ledger(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.ledger(accountId, from, to)));
    }

    @GetMapping("/libro-ventas")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> salesBook(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.salesBook(from, to)));
    }

    @GetMapping("/libro-compras")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> purchasesBook(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.purchasesBook(from, to)));
    }
}
