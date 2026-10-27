package com.coteja.erp.module.auth.controller;

import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.repository.UserRepository;
import com.coteja.erp.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpSession session) {
        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("No autenticado"));
        }

        var user = userRepository.findByEmail(principal.getEmail()).orElse(null);
        Long companyId = (Long) session.getAttribute("company_id");

        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("id", principal.getId());
        data.put("email", principal.getEmail());
        data.put("name", user != null ? user.getName() : principal.getEmail());
        data.put("role", principal.getRole().name());
        data.put("moduleAccesses", principal.getModuleAccesses());
        data.put("companyId", companyId);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        TenantContext.clear();
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
