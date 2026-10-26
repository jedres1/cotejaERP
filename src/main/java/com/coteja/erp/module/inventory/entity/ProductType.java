package com.coteja.erp.module.inventory.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_types")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductType extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    private String code;

    @Column(name = "controls_inventory")
    @Builder.Default
    private boolean controlsInventory = true;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;
}
