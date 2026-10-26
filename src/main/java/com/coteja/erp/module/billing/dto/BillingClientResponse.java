package com.coteja.erp.module.billing.dto;

import com.coteja.erp.module.billing.entity.BillingClient;

public record BillingClientResponse(
        Long id,
        String name,
        String tradeName,
        String documentType,
        String documentNumber,
        String nrc,
        String businessActivity,
        String email,
        String phone,
        String billingEmail,
        String billingPhone,
        String addressDepartment,
        String addressMunicipality,
        String addressDistrict,
        String address,
        String preferredDteType,
        boolean clientsVarios
) {
    public static BillingClientResponse from(BillingClient c) {
        return new BillingClientResponse(
                c.getId(), c.getName(), c.getTradeName(),
                c.getDocumentType(), c.getDocumentNumber(), c.getNrc(),
                c.getBusinessActivity(), c.getEmail(), c.getPhone(),
                c.getBillingEmail(), c.getBillingPhone(),
                c.getAddressDepartment(), c.getAddressMunicipality(),
                c.getAddressDistrict(), c.getAddress(),
                c.getPreferredDteType(), c.isClientsVarios()
        );
    }
}
