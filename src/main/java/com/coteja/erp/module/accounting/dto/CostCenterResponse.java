package com.coteja.erp.module.accounting.dto;

import com.coteja.erp.module.accounting.entity.CostCenter;

public record CostCenterResponse(
        Long id,
        String code,
        String name,
        String description,
        Long parentId,
        String parentName,
        Integer level,
        boolean isActive
) {
    public static CostCenterResponse from(CostCenter c) {
        return new CostCenterResponse(c.getId(), c.getCode(), c.getName(), c.getDescription(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getParent() != null ? c.getParent().getName() : null,
                c.getLevel(), c.isActive());
    }
}
