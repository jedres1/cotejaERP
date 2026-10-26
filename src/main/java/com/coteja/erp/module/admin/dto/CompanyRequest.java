package com.coteja.erp.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CompanyRequest(
        @NotNull(message = "El cliente es obligatorio")
        Long customerId,
        @NotBlank(message = "La razón social es obligatoria")
        String businessName,
        String tradeName,
        String nitDui,
        String nrc,
        String tipoPersona,
        String businessActivity,
        String addressDepartment,
        String addressMunicipality,
        String addressDistrict,
        String address,
        String email,
        String phone,
        String environment,
        List<String> moduleAccesses
) {}
