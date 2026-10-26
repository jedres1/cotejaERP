package com.coteja.erp.module.purchases.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "purchase_invoices")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PurchaseInvoice extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "document_type")
    @Builder.Default
    private String documentType = "03";

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal iva = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    @Builder.Default
    private String paymentStatus = "pending";

    @Builder.Default
    private String status = "registered";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_document_body", columnDefinition = "json")
    private Map<String, Object> extractedDocumentBody;

    public String getReceptorName() {
        if (extractedDocumentBody != null && extractedDocumentBody.containsKey("receptor")) {
            Object receptor = extractedDocumentBody.get("receptor");
            if (receptor instanceof Map<?, ?> map) {
                Object nombre = map.get("nombre");
                return nombre != null ? nombre.toString() : null;
            }
        }
        return supplier != null ? supplier.getName() : null;
    }

    public BigDecimal getBalance() {
        return total;
    }
}
