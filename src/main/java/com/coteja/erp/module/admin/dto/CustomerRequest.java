package com.coteja.erp.module.admin.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CustomerRequest(
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
        String address,
        String preferredDteType,
        String billingEmail,
        String billingPhone,
        String notes,
        List<String> moduleAccesses
) {}
