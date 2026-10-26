package com.coteja.erp.module.admin.dto;

import com.coteja.erp.module.admin.entity.AppRelease;

import java.time.LocalDateTime;

public record AppReleaseResponse(
        Long id,
        String platform,
        String version,
        String filename,
        String downloadUrl,
        String notes,
        boolean isActive,
        LocalDateTime createdAt
) {
    public static AppReleaseResponse from(AppRelease r) {
        return new AppReleaseResponse(
                r.getId(), r.getPlatform(), r.getVersion(),
                r.getFilename(), r.getDownloadUrl(), r.getNotes(),
                r.isActive(), r.getCreatedAt()
        );
    }
}
