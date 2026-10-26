package com.coteja.erp.module.admin.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.admin.dto.CompanyRequest;
import com.coteja.erp.module.admin.dto.CompanyResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.entity.Customer;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.admin.repository.CustomerRepository;
import com.coteja.erp.module.billing.entity.BillingSetting;
import com.coteja.erp.module.billing.repository.BillingSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final BillingSettingRepository billingSettingRepository;

    @Transactional(readOnly = true)
    public PageResponse<CompanyResponse> findAll(Pageable pageable) {
        return PageResponse.from(
                companyRepository.findAll(pageable).map(CompanyResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        return companyRepository.findById(id)
                .map(CompanyResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> findByCustomerId(Long customerId) {
        return companyRepository.findByCustomerId(customerId)
                .stream().map(CompanyResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> findByUserId(Long userId) {
        return companyRepository.findByUserId(userId)
                .stream().map(CompanyResponse::from).toList();
    }

    public CompanyResponse create(CompanyRequest request) {
        Customer customer = customerRepository.findByIdAndDeletedAtIsNull(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        Company company = Company.builder()
                .customer(customer)
                .businessName(request.businessName())
                .tradeName(request.tradeName())
                .nitDui(request.nitDui())
                .nrc(request.nrc())
                .tipoPersona(request.tipoPersona())
                .businessActivity(request.businessActivity())
                .addressDepartment(request.addressDepartment())
                .addressMunicipality(request.addressMunicipality())
                .addressDistrict(request.addressDistrict())
                .address(request.address())
                .email(request.email())
                .phone(request.phone())
                .environment(request.environment() != null ? request.environment() : "production")
                .moduleAccesses(request.moduleAccesses())
                .build();
        Company saved = companyRepository.save(company);
        syncBillingSettings(saved.getId());
        return CompanyResponse.from(saved);
    }

    public CompanyResponse update(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        company.setBusinessName(request.businessName());
        company.setTradeName(request.tradeName());
        company.setNitDui(request.nitDui());
        company.setNrc(request.nrc());
        company.setTipoPersona(request.tipoPersona());
        company.setBusinessActivity(request.businessActivity());
        company.setAddressDepartment(request.addressDepartment());
        company.setAddressMunicipality(request.addressMunicipality());
        company.setAddressDistrict(request.addressDistrict());
        company.setAddress(request.address());
        company.setEmail(request.email());
        company.setPhone(request.phone());
        if (request.environment() != null) company.setEnvironment(request.environment());
        if (request.moduleAccesses() != null) company.setModuleAccesses(request.moduleAccesses());
        Company saved = companyRepository.save(company);
        syncBillingSettings(saved.getId());
        return CompanyResponse.from(saved);
    }

    public void syncBillingSettings(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        Map<String, Object> emisor = new LinkedHashMap<>();
        emisor.put("nit", company.getNitDui());
        emisor.put("nrc", company.getNrc());
        emisor.put("nombre", company.getBusinessName());
        emisor.put("nombreComercial", company.getTradeName());
        emisor.put("codActividad", company.getBusinessActivity());
        emisor.put("descActividad", company.getBusinessActivity());
        Map<String, Object> dir = new LinkedHashMap<>();
        dir.put("departamento", company.getAddressDepartment());
        dir.put("municipio", company.getAddressMunicipality());
        dir.put("complemento", company.getAddress());
        emisor.put("direccion", dir);
        emisor.put("telefono", company.getPhone());
        emisor.put("correo", company.getEmail());
        emisor.put("tipoEstablecimiento", "20");
        emisor.put("codEstableMH", null);
        emisor.put("codEstable", "0001");
        emisor.put("codPuntoVentaMH", null);
        emisor.put("codPuntoVenta", "0001");

        BillingSetting setting = billingSettingRepository
                .findByCompanyIdAndKey(companyId, "emisor")
                .orElseGet(() -> BillingSetting.builder().company(company).key("emisor").build());
        setting.setValue(emisor);
        billingSettingRepository.save(setting);
    }
}
