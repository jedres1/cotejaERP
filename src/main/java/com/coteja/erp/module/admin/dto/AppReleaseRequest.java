package com.coteja.erp.module.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AppReleaseRequest(
        @NotBlank String platform,
        @NotBlank String version,
        String filename,
        String downloadUrl,
        String notes,
        Boolean isActive
) {}
