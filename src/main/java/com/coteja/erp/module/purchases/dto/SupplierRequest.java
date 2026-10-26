package com.coteja.erp.module.purchases.dto;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        String email,
        String phone,
        String documentType,
        String documentNumber,
        String nrc,
        String tradeName,
        String businessActivity,
        String activityDescription,
        String addressDepartment,
        String addressMunicipality,
        String addressDistrict,
        String address,
        String billingEmail,
        String billingPhone,
        String notes
) {}
