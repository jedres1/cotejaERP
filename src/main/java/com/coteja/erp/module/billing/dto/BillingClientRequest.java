package com.coteja.erp.module.billing.dto;

import jakarta.validation.constraints.NotBlank;

public record BillingClientRequest(
        @NotBlank String name,
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
        Boolean clientsVarios
) {}
