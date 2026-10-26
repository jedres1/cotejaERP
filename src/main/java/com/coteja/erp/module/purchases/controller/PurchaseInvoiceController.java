package com.coteja.erp.module.purchases.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.purchases.dto.PurchaseInvoiceRequest;
import com.coteja.erp.module.purchases.dto.PurchaseInvoiceResponse;
import com.coteja.erp.module.purchases.service.PurchaseInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/purchase-invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','INQUILINO') and @moduleAccess.hasAccess(authentication,'purchases')")
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PurchaseInvoiceResponse>>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                invoiceService.findAll(PageRequest.of(page, size, Sort.by("purchaseDate").descending()))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> show(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> store(
            @Valid @RequestBody PurchaseInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Factura registrada", invoiceService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> update(
            @PathVariable Long id, @Valid @RequestBody PurchaseInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Factura actualizada", invoiceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> destroy(@PathVariable Long id) {
        invoiceService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok("Factura eliminada", null));
    }

    @GetMapping("/pendientes-aprobacion")
    public ResponseEntity<ApiResponse<List<PurchaseInvoiceResponse>>> pendingApproval() {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.findPendingApproval()));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long id) {
        invoiceService.approve(id);
        return ResponseEntity.ok(ApiResponse.ok("Factura aprobada", null));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long id) {
        invoiceService.reject(id);
        return ResponseEntity.ok(ApiResponse.ok("Factura rechazada", null));
    }

    @PostMapping("/aprobar-masivo")
    public ResponseEntity<ApiResponse<Void>> approveBulk(@RequestBody List<Long> ids) {
        invoiceService.approveBulk(ids);
        return ResponseEntity.ok(ApiResponse.ok("Facturas aprobadas", null));
    }

    @PostMapping("/rechazar-masivo")
    public ResponseEntity<ApiResponse<Void>> rejectBulk(@RequestBody List<Long> ids) {
        invoiceService.rejectBulk(ids);
        return ResponseEntity.ok(ApiResponse.ok("Facturas rechazadas", null));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<Void>> markPaid(@PathVariable Long id) {
        invoiceService.markPaid(id);
        return ResponseEntity.ok(ApiResponse.ok("Factura marcada como pagada", null));
    }

    @GetMapping("/cuentas-por-pagar")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseInvoiceResponse>>> accountsPayable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                invoiceService.findAccountsPayable(PageRequest.of(page, size))
        ));
    }
}
