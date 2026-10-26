package com.coteja.erp.module.admin.dto;

import com.coteja.erp.module.admin.entity.User;
import com.coteja.erp.module.admin.entity.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        boolean isActive,
        Long customerId,
        String customerName,
        List<String> moduleAccesses,
        LocalDateTime createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive(),
                u.getCustomer() != null ? u.getCustomer().getId() : null,
                u.getCustomer() != null ? u.getCustomer().getName() : null,
                u.getModuleAccesses(), u.getCreatedAt()
        );
    }
}
