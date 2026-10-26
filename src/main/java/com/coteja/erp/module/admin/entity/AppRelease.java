package com.coteja.erp.module.admin.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_releases")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppRelease extends AuditableEntity {

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String version;

    private String filename;

    @Column(name = "download_url")
    private String downloadUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;
}
