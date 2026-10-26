package com.coteja.erp.module.admin.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "nit_dui")
    private String nitDui;

    private String nrc;

    @Column(name = "tipo_persona")
    private String tipoPersona;

    @Column(name = "business_activity")
    private String businessActivity;

    @Column(name = "address_department")
    private String addressDepartment;

    @Column(name = "address_municipality")
    private String addressMunicipality;

    @Column(name = "address_district")
    private String addressDistrict;

    private String address;
    private String email;
    private String phone;

    @Column(nullable = false)
    @Builder.Default
    private String environment = "production";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "module_accesses", columnDefinition = "json")
    @Builder.Default
    private List<String> moduleAccesses = new ArrayList<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @Builder.Default
    private List<License> licenses = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "company_user",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> users = new ArrayList<>();

    public License getActiveLicense() {
        return licenses.stream()
                .filter(License::isActive)
                .findFirst()
                .orElse(null);
    }
}
