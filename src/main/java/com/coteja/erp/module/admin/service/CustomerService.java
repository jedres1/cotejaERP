package com.coteja.erp.module.admin.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.admin.dto.CustomerRequest;
import com.coteja.erp.module.admin.dto.CustomerResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.entity.Customer;
import com.coteja.erp.module.admin.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> findAll(Pageable pageable) {
        return PageResponse.from(
                customerRepository.findAllByDeletedAtIsNull(pageable)
                        .map(CustomerResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return customerRepository.findByIdAndDeletedAtIsNull(id)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    public CustomerResponse create(CustomerRequest request) {
        Customer customer = Customer.builder()
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
                .address(request.address())
                .preferredDteType(request.preferredDteType())
                .billingEmail(request.billingEmail())
                .billingPhone(request.billingPhone())
                .notes(request.notes())
                .moduleAccesses(request.moduleAccesses())
                .build();
        return CustomerResponse.from(customerRepository.save(customer));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setDocumentType(request.documentType());
        customer.setDocumentNumber(request.documentNumber());
        customer.setNrc(request.nrc());
        customer.setTradeName(request.tradeName());
        customer.setBusinessActivity(request.businessActivity());
        customer.setActivityDescription(request.activityDescription());
        customer.setAddressDepartment(request.addressDepartment());
        customer.setAddressMunicipality(request.addressMunicipality());
        customer.setAddress(request.address());
        customer.setPreferredDteType(request.preferredDteType());
        customer.setBillingEmail(request.billingEmail());
        customer.setBillingPhone(request.billingPhone());
        customer.setNotes(request.notes());
        if (request.moduleAccesses() != null) {
            customer.setModuleAccesses(request.moduleAccesses());
        }
        return CustomerResponse.from(customerRepository.save(customer));
    }

    public void softDelete(Long id) {
        Customer customer = customerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }
}
