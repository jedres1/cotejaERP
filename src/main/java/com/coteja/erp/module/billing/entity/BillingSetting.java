package com.coteja.erp.module.billing.entity;

import com.coteja.erp.module.admin.entity.Company;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "billing_settings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "`key`"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BillingSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "`key`", nullable = false)
    private String key;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> value;
}
