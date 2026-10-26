package com.coteja.erp.module.billing.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "billing_invoices")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BillingInvoice extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_customer_id")
    private Customer tenantCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private BillingClient client;

    @Column(name = "number_control", unique = true)
    private String numberControl;

    @Column(name = "generation_code", unique = true)
    private String generationCode;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    @Column(name = "customer_name")
    private String customerName;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal iva = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Builder.Default
    private String status = "PENDIENTE";

    @Column(name = "has_error")
    @Builder.Default
    private boolean hasError = false;

    @Builder.Default
    private boolean accepted = false;

    @Column(name = "email_sent")
    @Builder.Default
    private boolean emailSent = false;

    @Column(name = "reception_stamp", columnDefinition = "TEXT")
    private String receptionStamp;

    @Column(name = "void_stamp", columnDefinition = "TEXT")
    private String voidStamp;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "void_reason", columnDefinition = "TEXT")
    private String voidReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "void_json", columnDefinition = "JSON")
    private Map<String, Object> voidJson;

    @Column(name = "return_stamp", columnDefinition = "TEXT")
    private String returnStamp;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "return_json", columnDefinition = "JSON")
    private Map<String, Object> returnJson;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_dte", columnDefinition = "JSON")
    private Map<String, Object> jsonDte;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "signed_dte", columnDefinition = "JSON")
    private Map<String, Object> signedDte;

    @Column(name = "payment_status")
    @Builder.Default
    private String paymentStatus = "pending";

    @Column(name = "amount_paid")
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BillingInvoicePayment> payments = new ArrayList<>();

    public BigDecimal getBalance() {
        return total.subtract(amountPaid);
    }

    public boolean isVoidable() {
        return "ACEPTADO".equals(status) || "PENDIENTE".equals(status);
    }
}
