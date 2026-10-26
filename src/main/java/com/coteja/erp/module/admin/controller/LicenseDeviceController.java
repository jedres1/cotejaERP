package com.coteja.erp.module.admin.controller;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.entity.License;
import com.coteja.erp.module.admin.entity.LicenseDevice;
import com.coteja.erp.module.admin.repository.LicenseDeviceRepository;
import com.coteja.erp.module.admin.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dispositivos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LicenseDeviceController {

    private final LicenseDeviceRepository deviceRepository;
    private final LicenseRepository licenseRepository;

    @GetMapping("/licencia/{licenseId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listByLicense(@PathVariable Long licenseId) {
        List<Map<String, Object>> result = deviceRepository.findByLicenseId(licenseId).stream()
                .map(d -> Map.<String, Object>of(
                        "id", d.getId(),
                        "deviceId", d.getDeviceId(),
                        "deviceName", d.getDeviceName() != null ? d.getDeviceName() : "",
                        "isActive", d.isActive(),
                        "activatedAt", d.getActivatedAt() != null ? d.getActivatedAt().toString() : "",
                        "lastSeenAt", d.getLastSeenAt() != null ? d.getLastSeenAt().toString() : ""
                )).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        LicenseDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LicenseDevice", id));
        device.setActive(true);
        device.setActivatedAt(LocalDateTime.now());
        deviceRepository.save(device);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        LicenseDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LicenseDevice", id));
        device.setActive(false);
        deviceRepository.save(device);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!deviceRepository.existsById(id)) throw new ResourceNotFoundException("LicenseDevice", id);
        deviceRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
