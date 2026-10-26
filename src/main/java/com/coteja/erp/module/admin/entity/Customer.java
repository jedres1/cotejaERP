package com.coteja.erp.module.admin.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    private String nrc;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "business_activity")
    private String businessActivity;

    @Column(name = "activity_description", columnDefinition = "TEXT")
    private String activityDescription;

    @Column(name = "address_department")
    private String addressDepartment;

    @Column(name = "address_municipality")
    private String addressMunicipality;

    private String address;

    @Column(name = "preferred_dte_type")
    private String preferredDteType;

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "billing_phone")
    private String billingPhone;

    @Column(nullable = false)
    @Builder.Default
    private String status = "active";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "module_accesses", columnDefinition = "json")
    @Builder.Default
    private List<String> moduleAccesses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Company> companies = new ArrayList<>();

    public Map<String, Object> toDteReceptor() {
        Map<String, Object> receptor = new HashMap<>();
        receptor.put("nombre", name);
        receptor.put("tipoDocumento", documentType);
        receptor.put("numDocumento", documentNumber);
        receptor.put("nrc", nrc);
        receptor.put("correoElectronico", billingEmail != null ? billingEmail : email);
        receptor.put("telefono", billingPhone != null ? billingPhone : phone);
        receptor.put("direccion", address);
        receptor.put("departamento", addressDepartment);
        receptor.put("municipio", addressMunicipality);
        return receptor;
    }
}
