package com.coteja.erp.module.accounting.entity;

import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounting_year_periods")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountingYearPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private Integer year;

    private String name;

    @Builder.Default
    private String status = "abierto";

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    private User closedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean isOpen() {
        return "abierto".equals(status);
    }
}
