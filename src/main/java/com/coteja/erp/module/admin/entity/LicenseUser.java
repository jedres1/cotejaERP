package com.coteja.erp.module.admin.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "license_users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseUser extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @Column(nullable = false)
    private String name;

    private String email;

    @Builder.Default
    private String role = "operator";

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;
}
