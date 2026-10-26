package com.coteja.erp.module.billing.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import com.coteja.erp.module.inventory.entity.ProductType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "billing_products")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BillingProduct extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** bien o servicio */
    private String type;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    private String unit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> tributes;

    @Column(name = "is_exempt")
    @Builder.Default
    private boolean exempt = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "min_stock")
    @Builder.Default
    private Integer minStock = 0;

    public Map<String, Object> toInvoiceItem(BigDecimal quantity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("codigo", code);
        item.put("descripcion", description);
        item.put("cantidad", quantity);
        item.put("uniMedida", unit != null ? unit : "59");
        item.put("precioUni", price);
        item.put("montoDescu", BigDecimal.ZERO);
        BigDecimal ventaGravada = exempt ? BigDecimal.ZERO : price.multiply(quantity);
        BigDecimal ventaExenta = exempt ? price.multiply(quantity) : BigDecimal.ZERO;
        item.put("ventaGravada", ventaGravada);
        item.put("ventaExenta", ventaExenta);
        item.put("tributos", tributes);
        return item;
    }
}
