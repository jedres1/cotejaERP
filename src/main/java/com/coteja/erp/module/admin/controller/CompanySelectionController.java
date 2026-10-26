package com.coteja.erp.module.admin.controller;

import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.CompanyResponse;
import com.coteja.erp.module.admin.service.CompanyService;
import com.coteja.erp.security.UserPrincipal;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/empresa")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','INQUILINO')")
public class CompanySelectionController {

    private final CompanyService companyService;

    @GetMapping("/seleccionar")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> listarEmpresas(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CompanyResponse> companies = principal.isAdmin()
                ? companyService.findAll(org.springframework.data.domain.PageRequest.of(0, 100)).content()
                : companyService.findByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(companies));
    }

    @PostMapping("/cambiar")
    public ResponseEntity<ApiResponse<Void>> cambiarEmpresa(
            @RequestBody Map<String, Long> body,
            HttpSession session) {
        Long companyId = body.get("companyId");
        if (companyId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("companyId requerido"));
        }
        session.setAttribute("company_id", companyId);
        TenantContext.setCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.ok("Empresa seleccionada", null));
    }

    @PostMapping("/salir")
    public ResponseEntity<ApiResponse<Void>> salirEmpresa(HttpSession session) {
        session.removeAttribute("company_id");
        TenantContext.clear();
        return ResponseEntity.ok(ApiResponse.ok("Sesión de empresa cerrada", null));
    }
}
