package com.coteja.erp.module.admin.controller;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.entity.Backup;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.License;
import com.coteja.erp.module.admin.repository.BackupRepository;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.admin.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/backups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupRepository backupRepository;
    private final LicenseRepository licenseRepository;
    private final CompanyRepository companyRepository;

    @GetMapping("/empresa/{companyId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listByCompany(@PathVariable Long companyId) {
        List<Map<String, Object>> result = backupRepository.findByCompanyIdOrderByUploadedAtDesc(companyId).stream()
                .map(b -> Map.<String, Object>of(
                        "id", b.getId(),
                        "filename", b.getFilename(),
                        "sizeBytes", b.getSizeBytes() != null ? b.getSizeBytes() : 0,
                        "deviceId", b.getDeviceId() != null ? b.getDeviceId() : "",
                        "uploadedAt", b.getUploadedAt(),
                        "encryption", b.getEncryption(),
                        "compression", b.getCompression()
                )).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody Map<String, Object> body) {
        Long licenseId  = Long.parseLong(body.get("licenseId").toString());
        Long companyId  = Long.parseLong(body.get("companyId").toString());
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResourceNotFoundException("License", licenseId));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        Backup backup = Backup.builder()
                .license(license)
                .company(company)
                .filename((String) body.get("filename"))
                .path((String) body.get("path"))
                .sizeBytes(body.get("sizeBytes") != null ? Long.parseLong(body.get("sizeBytes").toString()) : null)
                .checksum((String) body.get("checksum"))
                .deviceId((String) body.get("deviceId"))
                .uploadedAt(LocalDateTime.now())
                .build();

        Backup saved = backupRepository.save(backup);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("id", saved.getId(), "filename", saved.getFilename())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!backupRepository.existsById(id)) throw new ResourceNotFoundException("Backup", id);
        backupRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
