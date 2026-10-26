package com.coteja.erp.module.admin.dto;

import com.coteja.erp.module.admin.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        String email,
        String password,
        @NotNull(message = "El rol es obligatorio")
        UserRole role,
        Long customerId,
        Boolean isActive,
        List<String> moduleAccesses
) {}
