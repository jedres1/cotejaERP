package com.coteja.erp.module.admin.controller;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.License;
import com.coteja.erp.module.admin.entity.Payment;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.admin.repository.LicenseRepository;
import com.coteja.erp.module.admin.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/pagos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final LicenseRepository licenseRepository;
    private final CompanyRepository companyRepository;

    @GetMapping("/empresa/{companyId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listByCompany(@PathVariable Long companyId) {
        List<Map<String, Object>> result = paymentRepository.findByCompanyId(companyId).stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "amount", p.getAmount(),
                        "iva", p.getIva(),
                        "total", p.getTotal(),
                        "period", p.getPeriod(),
                        "method", p.getMethod() != null ? p.getMethod() : "",
                        "reference", p.getReference() != null ? p.getReference() : "",
                        "status", p.getStatus(),
                        "paidAt", p.getPaidAt() != null ? p.getPaidAt().toString() : ""
                )).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody Map<String, Object> body) {
        Long licenseId = Long.parseLong(body.get("licenseId").toString());
        Long companyId = Long.parseLong(body.get("companyId").toString());
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResourceNotFoundException("License", licenseId));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        BigDecimal iva    = body.get("iva") != null ? new BigDecimal(body.get("iva").toString()) : BigDecimal.ZERO;

        Payment payment = Payment.builder()
                .license(license)
                .company(company)
                .amount(amount)
                .iva(iva)
                .total(amount.add(iva))
                .period((String) body.getOrDefault("period", "monthly"))
                .method((String) body.get("method"))
                .reference((String) body.get("reference"))
                .status("paid")
                .paidAt(LocalDate.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("id", saved.getId(), "total", saved.getTotal())));
    }
}
