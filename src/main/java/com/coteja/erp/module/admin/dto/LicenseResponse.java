package com.coteja.erp.module.admin.dto;

import com.coteja.erp.module.admin.entity.License;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LicenseResponse(
        Long id,
        Long companyId,
        String companyName,
        Long planId,
        String planName,
        String licenseKey,
        String licenseType,
        String status,
        LocalDate startsAt,
        LocalDate expiresAt,
        Integer maxUsers,
        Integer maxDevices,
        Integer graceDays,
        boolean isActive,
        LocalDateTime lastValidatedAt
) {
    public static LicenseResponse from(License l) {
        return new LicenseResponse(
                l.getId(),
                l.getCompany() != null ? l.getCompany().getId() : null,
                l.getCompany() != null ? l.getCompany().getBusinessName() : null,
                l.getPlan() != null ? l.getPlan().getId() : null,
                l.getPlan() != null ? l.getPlan().getName() : null,
                l.getLicenseKey(), l.getLicenseType(), l.getStatus(),
                l.getStartsAt(), l.getExpiresAt(),
                l.getMaxUsers(), l.getMaxDevices(), l.getGraceDays(),
                l.isActive(), l.getLastValidatedAt()
        );
    }
}
