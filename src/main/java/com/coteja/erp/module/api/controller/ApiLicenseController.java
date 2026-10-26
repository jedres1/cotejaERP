package com.coteja.erp.module.api.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.LicenseResponse;
import com.coteja.erp.module.admin.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/licenses")
@RequiredArgsConstructor
public class ApiLicenseController {

    private final LicenseService licenseService;

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<LicenseResponse>> validate(
            @RequestBody Map<String, String> body) {
        String licenseKey = body.get("licenseKey");
        String deviceId = body.get("deviceId");
        if (licenseKey == null || deviceId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("licenseKey y deviceId son requeridos"));
        }
        return ResponseEntity.ok(licenseService.validateLicense(licenseKey, deviceId));
    }
}
