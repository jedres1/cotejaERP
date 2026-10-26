package com.coteja.erp.module.billing.dto;

import com.coteja.erp.module.billing.entity.BillingProduct;

import java.math.BigDecimal;
import java.util.List;

public record BillingProductResponse(
        Long id,
        String code,
        String description,
        String type,
        BigDecimal price,
        String unit,
        Long productTypeId,
        String productTypeName,
        List<String> tributes,
        boolean exempt,
        String notes,
        Integer stockQuantity,
        Integer minStock
) {
    public static BillingProductResponse from(BillingProduct p) {
        return new BillingProductResponse(
                p.getId(), p.getCode(), p.getDescription(), p.getType(),
                p.getPrice(), p.getUnit(),
                p.getProductType() != null ? p.getProductType().getId() : null,
                p.getProductType() != null ? p.getProductType().getName() : null,
                p.getTributes(), p.isExempt(), p.getNotes(),
                p.getStockQuantity(), p.getMinStock()
        );
    }
}
