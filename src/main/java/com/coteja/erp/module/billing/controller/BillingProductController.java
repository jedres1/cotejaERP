package com.coteja.erp.module.billing.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.billing.dto.BillingProductRequest;
import com.coteja.erp.module.billing.dto.BillingProductResponse;
import com.coteja.erp.module.billing.service.BillingProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/factura-sv/productos")
@RequiredArgsConstructor
@PreAuthorize("@moduleAccess.hasAccess(authentication,'billing')")
public class BillingProductController {

    private final BillingProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BillingProductResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("code").ascending());
        return ResponseEntity.ok(ApiResponse.ok(productService.findAll(pageable)));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<BillingProductResponse>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(productService.search(q)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillingProductResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BillingProductResponse>> create(
            @Valid @RequestBody BillingProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillingProductResponse>> update(
            @PathVariable Long id, @Valid @RequestBody BillingProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
