package com.coteja.erp.module.billing.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.billing.dto.BillingProductRequest;
import com.coteja.erp.module.billing.dto.BillingProductResponse;
import com.coteja.erp.module.billing.entity.BillingProduct;
import com.coteja.erp.module.billing.repository.BillingProductRepository;
import com.coteja.erp.module.inventory.entity.ProductType;
import com.coteja.erp.module.inventory.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingProductService {

    private final BillingProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public PageResponse<BillingProductResponse> findAll(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return PageResponse.from(
                productRepository.findByCompanyId(companyId, pageable).map(BillingProductResponse::from));
    }

    @Transactional(readOnly = true)
    public List<BillingProductResponse> search(String q) {
        Long companyId = TenantContext.getCompanyId();
        return productRepository.search(companyId, q).stream().map(BillingProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BillingProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(BillingProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("BillingProduct", id));
    }

    public BillingProductResponse create(BillingProductRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        ProductType productType = null;
        if (request.productTypeId() != null) {
            productType = productTypeRepository.findById(request.productTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductType", request.productTypeId()));
        }

        BillingProduct product = BillingProduct.builder()
                .customer(company.getCustomer())
                .company(company)
                .productType(productType)
                .code(request.code())
                .description(request.description())
                .type(request.type())
                .price(request.price())
                .unit(request.unit())
                .tributes(request.tributes())
                .exempt(Boolean.TRUE.equals(request.exempt()))
                .notes(request.notes())
                .build();

        return BillingProductResponse.from(productRepository.save(product));
    }

    public BillingProductResponse update(Long id, BillingProductRequest request) {
        BillingProduct product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingProduct", id));
        product.setCode(request.code());
        product.setDescription(request.description());
        product.setType(request.type());
        product.setPrice(request.price());
        product.setUnit(request.unit());
        product.setTributes(request.tributes());
        if (request.exempt() != null) product.setExempt(request.exempt());
        product.setNotes(request.notes());

        if (request.productTypeId() != null) {
            productTypeRepository.findById(request.productTypeId()).ifPresent(product::setProductType);
        }
        return BillingProductResponse.from(productRepository.save(product));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) throw new ResourceNotFoundException("BillingProduct", id);
        productRepository.deleteById(id);
    }
}
