package com.coteja.erp.module.accounting.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.accounting.dto.CostCenterRequest;
import com.coteja.erp.module.accounting.dto.CostCenterResponse;
import com.coteja.erp.module.accounting.entity.CostCenter;
import com.coteja.erp.module.accounting.repository.CostCenterRepository;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CostCenterService {

    private final CostCenterRepository costCenterRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<CostCenterResponse> findAll() {
        Long companyId = TenantContext.getCompanyId();
        return costCenterRepository.findByCompanyId(companyId)
                .stream().map(CostCenterResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CostCenterResponse findById(Long id) {
        return costCenterRepository.findById(id)
                .map(CostCenterResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("CostCenter", id));
    }

    public CostCenterResponse create(CostCenterRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        CostCenter parent = null;
        int level = 1;
        if (request.parentId() != null) {
            parent = costCenterRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("CostCenter", request.parentId()));
            level = parent.getLevel() + 1;
        }

        CostCenter cc = CostCenter.builder()
                .customer(company.getCustomer())
                .company(company)
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .parent(parent)
                .level(level)
                .active(request.isActive() == null || request.isActive())
                .build();

        return CostCenterResponse.from(costCenterRepository.save(cc));
    }

    public CostCenterResponse update(Long id, CostCenterRequest request) {
        CostCenter cc = costCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostCenter", id));
        cc.setCode(request.code());
        cc.setName(request.name());
        cc.setDescription(request.description());
        if (request.isActive() != null) cc.setActive(request.isActive());
        return CostCenterResponse.from(costCenterRepository.save(cc));
    }

    public void delete(Long id) {
        if (!costCenterRepository.existsById(id))
            throw new ResourceNotFoundException("CostCenter", id);
        costCenterRepository.deleteById(id);
    }
}
