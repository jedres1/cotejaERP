package com.coteja.erp.module.accounting.controller;

import com.coteja.erp.module.accounting.dto.JournalEntryRequest;
import com.coteja.erp.module.accounting.dto.JournalEntryResponse;
import com.coteja.erp.module.accounting.service.JournalEntryService;
import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/contabilidad/diario")
@RequiredArgsConstructor
@PreAuthorize("@moduleAccess.hasAccess(authentication,'accounting')")
public class JournalController {

    private final JournalEntryService journalEntryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JournalEntryResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("entryDate").descending().and(Sort.by("entryNumber").descending()));
        return ResponseEntity.ok(ApiResponse.ok(journalEntryService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(journalEntryService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JournalEntryResponse>> create(
            @Valid @RequestBody JournalEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(journalEntryService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> update(
            @PathVariable Long id, @Valid @RequestBody JournalEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(journalEntryService.update(id, request)));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long id) {
        journalEntryService.approve(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<ApiResponse<Void>> annul(@PathVariable Long id) {
        journalEntryService.annul(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        journalEntryService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
