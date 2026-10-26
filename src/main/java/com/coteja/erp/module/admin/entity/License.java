package com.coteja.erp.module.admin.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "licenses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class License extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "license_key", nullable = false, unique = true)
    private String licenseKey;

    @Column(name = "license_type")
    @Builder.Default
    private String licenseType = "propietario";

    @Column(nullable = false)
    @Builder.Default
    private String status = "active";

    @Column(name = "starts_at", nullable = false)
    private LocalDate startsAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDate expiresAt;

    @Column(name = "max_users")
    @Builder.Default
    private Integer maxUsers = 1;

    @Column(name = "max_devices")
    @Builder.Default
    private Integer maxDevices = 1;

    @Column(name = "grace_days")
    @Builder.Default
    private Integer graceDays = 0;

    @Column(name = "last_validated_at")
    private LocalDateTime lastValidatedAt;

    @OneToMany(mappedBy = "license", fetch = FetchType.LAZY)
    @Builder.Default
    private List<LicenseDevice> devices = new ArrayList<>();

    @OneToMany(mappedBy = "license", fetch = FetchType.LAZY)
    @Builder.Default
    private List<LicenseUser> licenseUsers = new ArrayList<>();

    @OneToMany(mappedBy = "license", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "license", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Backup> backups = new ArrayList<>();

    public boolean isActive() {
        return "active".equals(status) && expiresAt != null && !expiresAt.isBefore(LocalDate.now());
    }
}
