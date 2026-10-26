package com.coteja.erp.module.billing.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.billing.dto.BillingInvoiceRequest;
import com.coteja.erp.module.billing.dto.BillingInvoiceResponse;
import com.coteja.erp.module.billing.dto.PaymentRequest;
import com.coteja.erp.module.billing.service.BillingInvoiceService;
import com.coteja.erp.module.billing.service.DteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/factura-sv")
@RequiredArgsConstructor
@PreAuthorize("@moduleAccess.hasAccess(authentication,'billing')")
public class BillingInvoiceController {

    private final BillingInvoiceService invoiceService;
    private final DteService dteService;

    @GetMapping("/facturas")
    public ResponseEntity<ApiResponse<PageResponse<BillingInvoiceResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("issuedAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.findAll(pageable)));
    }

    @GetMapping("/facturas/{id}")
    public ResponseEntity<ApiResponse<BillingInvoiceResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.findById(id)));
    }

    @PostMapping("/procesar")
    public ResponseEntity<ApiResponse<BillingInvoiceResponse>> emitir(
            @Valid @RequestBody BillingInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.emitir(request)));
    }

    @PostMapping("/facturas/{id}/enviar")
    public ResponseEntity<ApiResponse<BillingInvoiceResponse>> enviar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.enviarAHacienda(id)));
    }

    @PostMapping("/facturas/{id}/anular")
    public ResponseEntity<ApiResponse<BillingInvoiceResponse>> anular(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String motivo = body.getOrDefault("motivo", "Anulación solicitada");
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.anular(id, motivo)));
    }

    @PostMapping("/facturas/{id}/pagar")
    public ResponseEntity<ApiResponse<BillingInvoiceResponse>> pagar(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.registrarPago(id, request)));
    }

    @PostMapping("/facturas/{id}/correo")
    public ResponseEntity<ApiResponse<Void>> enviarCorreo(@PathVariable Long id) {
        invoiceService.enviarCorreo(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/facturas/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        invoiceService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/cuentas-por-cobrar")
    public ResponseEntity<ApiResponse<List<BillingInvoiceResponse>>> cuentasPorCobrar() {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.findAccountsReceivable()));
    }

    // ── Firmador status ───────────────────────────────────────────────────────

    // ── Contingencia ──────────────────────────────────────────────────────────

    /**
     * Marks a pending invoice as sent under contingency mode
     * (when Hacienda's system is unavailable).
     */
    @PostMapping("/facturas/{id}/marcar-contingencia")
    public ResponseEntity<ApiResponse<BillingInvoiceResponse>> marcarContingencia(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String motivo = body.getOrDefault("motivo", "Sistema Hacienda no disponible");
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.marcarContingencia(id, motivo)));
    }

    /**
     * Emits a batch of invoices in contingency mode and schedules re-sending once Hacienda is back.
     */
    @PostMapping("/facturas/contingencia")
    public ResponseEntity<ApiResponse<List<BillingInvoiceResponse>>> emitirContingencia(
            @RequestBody List<Long> invoiceIds) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.emitirContingencia(invoiceIds)));
    }

    // ── Firmador status ───────────────────────────────────────────────────────

    @PostMapping("/firmador/estado")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estadoFirmador() {
        boolean activo = dteService.firmadorActivo();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("activo", activo)));
    }

    @PostMapping("/firmador/autenticar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> autenticarHacienda() {
        Long companyId = com.coteja.erp.common.tenant.TenantContext.getCompanyId();
        return ResponseEntity.ok(ApiResponse.ok(dteService.autenticarConHacienda(companyId)));
    }
}
