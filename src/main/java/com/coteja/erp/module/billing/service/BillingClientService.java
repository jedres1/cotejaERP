package com.coteja.erp.module.billing.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.billing.dto.BillingClientRequest;
import com.coteja.erp.module.billing.dto.BillingClientResponse;
import com.coteja.erp.module.billing.entity.BillingClient;
import com.coteja.erp.module.billing.repository.BillingClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingClientService {

    private final BillingClientRepository clientRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public PageResponse<BillingClientResponse> findAll(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return PageResponse.from(
                clientRepository.findByCompanyId(companyId, pageable).map(BillingClientResponse::from));
    }

    @Transactional(readOnly = true)
    public List<BillingClientResponse> search(String q) {
        Long companyId = TenantContext.getCompanyId();
        return clientRepository.search(companyId, q).stream().map(BillingClientResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BillingClientResponse findById(Long id) {
        return clientRepository.findById(id)
                .map(BillingClientResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("BillingClient", id));
    }

    public BillingClientResponse create(BillingClientRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        BillingClient client = BillingClient.builder()
                .customer(company.getCustomer())
                .company(company)
                .name(request.name())
                .tradeName(request.tradeName())
                .documentType(request.documentType())
                .documentNumber(request.documentNumber())
                .nrc(request.nrc())
                .businessActivity(request.businessActivity())
                .email(request.email())
                .phone(request.phone())
                .billingEmail(request.billingEmail())
                .billingPhone(request.billingPhone())
                .addressDepartment(request.addressDepartment())
                .addressMunicipality(request.addressMunicipality())
                .addressDistrict(request.addressDistrict())
                .address(request.address())
                .preferredDteType(request.preferredDteType())
                .clientsVarios(Boolean.TRUE.equals(request.clientsVarios()))
                .build();

        return BillingClientResponse.from(clientRepository.save(client));
    }

    public BillingClientResponse update(Long id, BillingClientRequest request) {
        BillingClient client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingClient", id));
        client.setName(request.name());
        client.setTradeName(request.tradeName());
        client.setDocumentType(request.documentType());
        client.setDocumentNumber(request.documentNumber());
        client.setNrc(request.nrc());
        client.setBusinessActivity(request.businessActivity());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client.setBillingEmail(request.billingEmail());
        client.setBillingPhone(request.billingPhone());
        client.setAddressDepartment(request.addressDepartment());
        client.setAddressMunicipality(request.addressMunicipality());
        client.setAddressDistrict(request.addressDistrict());
        client.setAddress(request.address());
        client.setPreferredDteType(request.preferredDteType());
        return BillingClientResponse.from(clientRepository.save(client));
    }

    public void delete(Long id) {
        if (!clientRepository.existsById(id)) throw new ResourceNotFoundException("BillingClient", id);
        clientRepository.deleteById(id);
    }
}
