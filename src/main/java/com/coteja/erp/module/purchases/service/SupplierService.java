package com.coteja.erp.module.purchases.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.admin.repository.CustomerRepository;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.purchases.dto.SupplierRequest;
import com.coteja.erp.module.purchases.dto.SupplierResponse;
import com.coteja.erp.module.purchases.entity.Supplier;
import com.coteja.erp.module.purchases.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> findAll(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return PageResponse.from(
                supplierRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                        .map(SupplierResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAllList() {
        Long companyId = TenantContext.getCompanyId();
        return supplierRepository.findByCompanyIdAndDeletedAtIsNull(companyId)
                .stream().map(SupplierResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return supplierRepository.findById(id)
                .map(SupplierResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    public SupplierResponse create(SupplierRequest request) {
        Long companyId = TenantContext.getCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        Supplier supplier = Supplier.builder()
                .customer(company.getCustomer())
                .company(company)
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .documentType(request.documentType())
                .documentNumber(request.documentNumber())
                .nrc(request.nrc())
                .tradeName(request.tradeName())
                .businessActivity(request.businessActivity())
                .activityDescription(request.activityDescription())
                .addressDepartment(request.addressDepartment())
                .addressMunicipality(request.addressMunicipality())
                .addressDistrict(request.addressDistrict())
                .address(request.address())
                .billingEmail(request.billingEmail())
                .billingPhone(request.billingPhone())
                .notes(request.notes())
                .build();
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        supplier.setName(request.name());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setDocumentType(request.documentType());
        supplier.setDocumentNumber(request.documentNumber());
        supplier.setNrc(request.nrc());
        supplier.setTradeName(request.tradeName());
        supplier.setBusinessActivity(request.businessActivity());
        supplier.setActivityDescription(request.activityDescription());
        supplier.setAddressDepartment(request.addressDepartment());
        supplier.setAddressMunicipality(request.addressMunicipality());
        supplier.setAddressDistrict(request.addressDistrict());
        supplier.setAddress(request.address());
        supplier.setBillingEmail(request.billingEmail());
        supplier.setBillingPhone(request.billingPhone());
        supplier.setNotes(request.notes());
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    public void softDelete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        supplier.setDeletedAt(LocalDateTime.now());
        supplierRepository.save(supplier);
    }
}
