package com.coteja.erp.module.admin.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "backups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Backup extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String filename;

    private String path;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    private String checksum;

    @Builder.Default
    private String encryption = "aes-256-gcm";

    @Builder.Default
    private String compression = "gzip";

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
