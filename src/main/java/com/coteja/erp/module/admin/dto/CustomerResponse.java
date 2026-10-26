package com.coteja.erp.module.admin.dto;

import com.coteja.erp.module.admin.entity.Customer;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerResponse(
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
        String notes,
        List<String> moduleAccesses,
        LocalDateTime createdAt
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(
                c.getId(), c.getName(), c.getEmail(), c.getPhone(),
                c.getDocumentType(), c.getDocumentNumber(), c.getNrc(),
                c.getTradeName(), c.getBusinessActivity(), c.getAddress(),
                c.getBillingEmail(), c.getBillingPhone(), c.getStatus(),
                c.getNotes(), c.getModuleAccesses(), c.getCreatedAt()
        );
    }
}
