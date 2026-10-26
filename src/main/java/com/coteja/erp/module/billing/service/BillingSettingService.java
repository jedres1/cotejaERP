package com.coteja.erp.module.billing.service;

import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.billing.entity.BillingSetting;
import com.coteja.erp.module.billing.repository.BillingSettingRepository;
import com.coteja.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingSettingService {

    private final BillingSettingRepository settingRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> findByKey(String key) {
        Long companyId = TenantContext.getCompanyId();
        return settingRepository.findByCompanyIdAndKey(companyId, key)
                .map(BillingSetting::getValue)
                .orElse(Map.of());
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Object>> findAll() {
        Long companyId = TenantContext.getCompanyId();
        Map<String, Map<String, Object>> result = new java.util.LinkedHashMap<>();
        settingRepository.findByCompanyId(companyId)
                .forEach(s -> result.put(s.getKey(), s.getValue()));
        return result;
    }

    public void upsert(String key, Map<String, Object> value) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        BillingSetting setting = settingRepository.findByCompanyIdAndKey(companyId, key)
                .orElseGet(() -> BillingSetting.builder().company(company).key(key).build());
        setting.setValue(value);
        settingRepository.save(setting);
    }

    public void delete(String key) {
        Long companyId = TenantContext.getCompanyId();
        settingRepository.findByCompanyIdAndKey(companyId, key)
                .ifPresent(settingRepository::delete);
    }
}
