package com.coteja.erp.module.billing.entity;

import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "billing_dte_correlatives",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "document_type", "year"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BillingDteCorrelative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(nullable = false)
    private Integer year;

    @Builder.Default
    private String establishment = "0001";

    @Column(name = "point_of_sale")
    @Builder.Default
    private String pointOfSale = "0001";

    @Column(name = "next_number")
    @Builder.Default
    private Long nextNumber = 1L;

    /** Returns next control number and increments counter — call inside PESSIMISTIC_WRITE tx. */
    public String generateNumberControl() {
        String numero = String.format("DTE%s-%s-%s-%015d",
                documentType, establishment, pointOfSale, nextNumber);
        nextNumber++;
        return numero;
    }
}
