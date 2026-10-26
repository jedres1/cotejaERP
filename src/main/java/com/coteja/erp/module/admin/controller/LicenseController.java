package com.coteja.erp.module.admin.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.LicenseRequest;
import com.coteja.erp.module.admin.dto.LicenseResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.service.LicenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/licenses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LicenseResponse>>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(licenseService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LicenseResponse>> show(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(licenseService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LicenseResponse>> store(@Valid @RequestBody LicenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Licencia creada", licenseService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LicenseResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LicenseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Licencia actualizada", licenseService.update(id, request)));
    }
}
