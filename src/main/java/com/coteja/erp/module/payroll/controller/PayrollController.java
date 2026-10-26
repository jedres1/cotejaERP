package com.coteja.erp.module.payroll.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.payroll.dto.*;
import com.coteja.erp.module.payroll.service.PayrollService;
import com.coteja.erp.module.purchases.service.PurchaseInvoiceMailboxImporter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/nomina")
@RequiredArgsConstructor
@PreAuthorize("@moduleAccess.hasAccess(authentication,'payroll')")
public class PayrollController {

    private final PayrollService payrollService;
    private final PurchaseInvoiceMailboxImporter mailboxImporter;

    // ── Empleados ────────────────────────────────────────────────────────────

    @GetMapping("/empleados")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> listEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(ApiResponse.ok(payrollService.findEmployees(pageable)));
    }

    @GetMapping("/empleados/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.findEmployeeById(id)));
    }

    @PostMapping("/empleados")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.createEmployee(request)));
    }

    @PutMapping("/empleados/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.updateEmployee(id, request)));
    }

    @DeleteMapping("/empleados/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        payrollService.softDeleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Conceptos de nómina ──────────────────────────────────────────────────

    @GetMapping("/conceptos")
    public ResponseEntity<ApiResponse<List<PayrollConceptResponse>>> listConcepts() {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.findConcepts()));
    }

    @PostMapping("/conceptos")
    public ResponseEntity<ApiResponse<PayrollConceptResponse>> createConcept(
            @Valid @RequestBody PayrollConceptRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.createConcept(request)));
    }

    @PutMapping("/conceptos/{id}")
    public ResponseEntity<ApiResponse<PayrollConceptResponse>> updateConcept(
            @PathVariable Long id, @Valid @RequestBody PayrollConceptRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.updateConcept(id, request)));
    }

    @DeleteMapping("/conceptos/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConcept(@PathVariable Long id) {
        payrollService.deleteConcept(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Configuración de nómina ──────────────────────────────────────────────

    @GetMapping("/configuracion")
    public ResponseEntity<ApiResponse<PayrollSettingsResponse>> getSettings() {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.findSettings()));
    }

    @PostMapping("/configuracion")
    public ResponseEntity<ApiResponse<PayrollSettingsResponse>> upsertSettings(
            @RequestBody PayrollSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.upsertSettings(request)));
    }

    // ── Períodos de nómina ───────────────────────────────────────────────────

    @GetMapping("/periodos")
    public ResponseEntity<ApiResponse<PageResponse<PayrollPeriodResponse>>> listPeriods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("periodStart").descending());
        return ResponseEntity.ok(ApiResponse.ok(payrollService.findPeriods(pageable)));
    }

    @GetMapping("/periodos/{id}")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> getPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.findPeriodById(id)));
    }

    @PostMapping("/periodos")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> createPeriod(
            @Valid @RequestBody PayrollPeriodRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.createPeriod(request)));
    }

    @PostMapping("/periodos/{id}/aplicar")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> applyPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(payrollService.applyPeriod(id)));
    }
}
