package com.coteja.erp.module.billing.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "billing_clients")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BillingClient extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    private String nrc;

    @Column(name = "business_activity")
    private String businessActivity;

    private String email;
    private String phone;

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "billing_phone")
    private String billingPhone;

    @Column(name = "address_department")
    private String addressDepartment;

    @Column(name = "address_municipality")
    private String addressMunicipality;

    @Column(name = "address_district")
    private String addressDistrict;

    private String address;

    @Column(name = "preferred_dte_type")
    private String preferredDteType;

    @Column(name = "is_clients_varios")
    @Builder.Default
    private boolean clientsVarios = false;

    /** Returns receptor structure expected by DTE JSON schema */
    public Map<String, Object> toDteReceptor() {
        Map<String, Object> receptor = new LinkedHashMap<>();
        receptor.put("nombre", name);
        receptor.put("tipoDocumento", documentType);
        receptor.put("numDocumento", documentNumber);
        receptor.put("nrc", nrc);
        receptor.put("nombreComercial", tradeName);
        receptor.put("codActividad", businessActivity);
        receptor.put("descActividad", businessActivity);
        Map<String, Object> direccion = new LinkedHashMap<>();
        direccion.put("departamento", addressDepartment);
        direccion.put("municipio", addressMunicipality);
        direccion.put("complemento", address);
        receptor.put("direccion", direccion);
        receptor.put("telefono", phone);
        receptor.put("correo", email);
        return receptor;
    }
}
