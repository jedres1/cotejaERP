package com.coteja.erp.module.admin.entity;

import com.coteja.erp.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.INQUILINO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "module_accesses", columnDefinition = "json")
    @Builder.Default
    private List<String> moduleAccesses = new ArrayList<>();

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "remember_token")
    private String rememberToken;

    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Company> companies = new ArrayList<>();

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(role);
    }

    public boolean isInquilino() {
        return UserRole.INQUILINO.equals(role);
    }

    public boolean hasModuleAccess(String module) {
        return moduleAccesses != null && moduleAccesses.contains(module);
    }
}
