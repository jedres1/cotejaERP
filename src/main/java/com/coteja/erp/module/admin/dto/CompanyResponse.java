package com.coteja.erp.module.admin.dto;

import com.coteja.erp.module.admin.entity.Company;

import java.time.LocalDateTime;
import java.util.List;

public record CompanyResponse(
        Long id,
        Long customerId,
        String customerName,
        String businessName,
        String tradeName,
        String nitDui,
        String nrc,
        String tipoPersona,
        String businessActivity,
        String address,
        String email,
        String phone,
        String environment,
        List<String> moduleAccesses,
        boolean hasActiveLicense,
        LocalDateTime createdAt
) {
    public static CompanyResponse from(Company c) {
        return new CompanyResponse(
                c.getId(),
                c.getCustomer() != null ? c.getCustomer().getId() : null,
                c.getCustomer() != null ? c.getCustomer().getName() : null,
                c.getBusinessName(), c.getTradeName(), c.getNitDui(), c.getNrc(),
                c.getTipoPersona(), c.getBusinessActivity(), c.getAddress(),
                c.getEmail(), c.getPhone(), c.getEnvironment(),
                c.getModuleAccesses(),
                c.getActiveLicense() != null,
                c.getCreatedAt()
        );
    }
}
