package com.coteja.erp.module.purchases.dto;

import com.coteja.erp.module.purchases.entity.Supplier;

import java.time.LocalDateTime;

public record SupplierResponse(
        Long id,
        String name,
        String email,
        String phone,
        String documentType,
        String documentNumber,
        String nrc,
        String tradeName,
        String businessActivity,
        String address,
        String billingEmail,
        String billingPhone,
        String status,
        LocalDateTime createdAt
) {
    public static SupplierResponse from(Supplier s) {
        return new SupplierResponse(
                s.getId(), s.getName(), s.getEmail(), s.getPhone(),
                s.getDocumentType(), s.getDocumentNumber(), s.getNrc(),
                s.getTradeName(), s.getBusinessActivity(), s.getAddress(),
                s.getBillingEmail(), s.getBillingPhone(), s.getStatus(),
                s.getCreatedAt()
        );
    }
}
