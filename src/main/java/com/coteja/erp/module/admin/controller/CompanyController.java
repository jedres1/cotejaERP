package com.coteja.erp.module.admin.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.CompanyRequest;
import com.coteja.erp.module.admin.dto.CompanyResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponse>>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(companyService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> show(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(companyService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> store(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Empresa creada", companyService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Empresa actualizada", companyService.update(id, request)));
    }
}
