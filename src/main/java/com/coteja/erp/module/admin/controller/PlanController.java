package com.coteja.erp.module.admin.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.PlanRequest;
import com.coteja.erp.module.admin.dto.PlanResponse;
import com.coteja.erp.module.admin.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanResponse>>> index() {
        return ResponseEntity.ok(ApiResponse.ok(planService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanResponse>> show(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(planService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PlanResponse>> store(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Plan creado", planService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Plan actualizado", planService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> destroy(@PathVariable Long id) {
        planService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Plan eliminado", null));
    }
}
