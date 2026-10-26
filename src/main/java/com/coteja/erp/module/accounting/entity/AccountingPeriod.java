package com.coteja.erp.module.accounting.entity;

import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Entity
@Table(name = "accounting_periods")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountingPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

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

    public LocalDate getStartDate() {
        return LocalDate.of(year, month, 1);
    }

    public LocalDate getEndDate() {
        return getStartDate().withDayOfMonth(getStartDate().lengthOfMonth());
    }

    public static String labelFor(int year, int month) {
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("es", "SV"));
        return capitalize(monthName) + " " + year;
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
