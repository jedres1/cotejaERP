package com.coteja.erp.module.billing.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.billing.service.BillingSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/factura-sv/configuracion")
@RequiredArgsConstructor
@PreAuthorize("@moduleAccess.hasAccess(authentication,'billing')")
public class BillingSettingController {

    private final BillingSettingService settingService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Map<String, Object>>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(settingService.findAll()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.ok(settingService.findByKey(key)));
    }

    @PostMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> upsert(
            @PathVariable String key,
            @RequestBody Map<String, Object> value) {
        settingService.upsert(key, value);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String key) {
        settingService.delete(key);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
