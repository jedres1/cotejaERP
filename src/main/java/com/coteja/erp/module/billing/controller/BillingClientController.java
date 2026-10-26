package com.coteja.erp.module.billing.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.billing.dto.BillingClientRequest;
import com.coteja.erp.module.billing.dto.BillingClientResponse;
import com.coteja.erp.module.billing.service.BillingClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/factura-sv/clientes")
@RequiredArgsConstructor
@PreAuthorize("@moduleAccess.hasAccess(authentication,'billing')")
public class BillingClientController {

    private final BillingClientService clientService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BillingClientResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(ApiResponse.ok(clientService.findAll(pageable)));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<BillingClientResponse>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.search(q)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillingClientResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BillingClientResponse>> create(
            @Valid @RequestBody BillingClientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillingClientResponse>> update(
            @PathVariable Long id, @Valid @RequestBody BillingClientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
