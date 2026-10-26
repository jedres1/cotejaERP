package com.coteja.erp.module.admin.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal monthlyPrice = BigDecimal.ZERO;

    @Column(name = "annual_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal annualPrice = BigDecimal.ZERO;

    @Column(name = "implementation_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal implementationFee = BigDecimal.ZERO;

    @Column(name = "additional_user_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal additionalUserPrice = BigDecimal.ZERO;

    @Column(name = "included_users")
    @Builder.Default
    private Integer includedUsers = 1;

    @Column(name = "max_devices")
    @Builder.Default
    private Integer maxDevices = 1;

    @Column(name = "support_included")
    @Builder.Default
    private boolean supportIncluded = false;

    @Column(name = "cloud_backup")
    @Builder.Default
    private boolean cloudBackup = false;

    @Column(name = "unlimited_documents")
    @Builder.Default
    private boolean unlimitedDocuments = false;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    @Builder.Default
    private List<License> licenses = new ArrayList<>();
}
