package com.coteja.erp.security;

import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("moduleAccess")
@RequiredArgsConstructor
public class ModuleAccessEvaluator {

    private final CompanyRepository companyRepository;

    public boolean hasAccess(Authentication authentication, String module) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            // Admin siempre tiene acceso
            if (principal.isAdmin()) {
                return true;
            }

            Long companyId = TenantContext.getCompanyId();
            if (companyId == null) {
                return false;
            }

            return companyRepository.findById(companyId)
                    .map(Company::getModuleAccesses)
                    .map(accesses -> accesses.contains(module))
                    .orElse(false);
        }

        return false;
    }
}
