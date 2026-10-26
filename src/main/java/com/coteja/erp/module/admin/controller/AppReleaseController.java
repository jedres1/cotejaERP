package com.coteja.erp.module.admin.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.AppReleaseRequest;
import com.coteja.erp.module.admin.dto.AppReleaseResponse;
import com.coteja.erp.module.admin.service.AppReleaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/releases")
@RequiredArgsConstructor
public class AppReleaseController {

    private final AppReleaseService releaseService;

    /** Public endpoint — clients call this to check for updates */
    @GetMapping("/latest/{platform}")
    public ResponseEntity<ApiResponse<AppReleaseResponse>> latest(@PathVariable String platform) {
        return ResponseEntity.ok(ApiResponse.ok(releaseService.findLatest(platform)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AppReleaseResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(releaseService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppReleaseResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(releaseService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppReleaseResponse>> create(
            @Valid @RequestBody AppReleaseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(releaseService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppReleaseResponse>> update(
            @PathVariable Long id, @Valid @RequestBody AppReleaseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(releaseService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        releaseService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
