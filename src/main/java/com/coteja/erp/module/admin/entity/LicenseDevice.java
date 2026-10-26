package com.coteja.erp.module.admin.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "license_devices", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"license_id", "device_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseDevice extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;
}
