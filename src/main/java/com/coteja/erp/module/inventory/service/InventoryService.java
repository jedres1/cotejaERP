package com.coteja.erp.module.inventory.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.inventory.dto.*;
import com.coteja.erp.module.inventory.entity.*;
import com.coteja.erp.module.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final ProductTypeRepository productTypeRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryMovementRepository movementRepository;
    private final CompanyRepository companyRepository;

    // ---- Product Types ----

    @Transactional(readOnly = true)
    public List<ProductTypeResponse> findAllProductTypes() {
        return productTypeRepository.findByCompanyIdAndActiveTrue(TenantContext.getCompanyId())
                .stream().map(ProductTypeResponse::from).toList();
    }

    public ProductTypeResponse createProductType(ProductTypeRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        ProductType pt = ProductType.builder()
                .customer(company.getCustomer())
                .company(company)
                .name(request.name())
                .code(request.code())
                .controlsInventory(request.controlsInventory() != null ? request.controlsInventory() : true)
                .active(request.isActive() == null || request.isActive())
                .build();
        return ProductTypeResponse.from(productTypeRepository.save(pt));
    }

    public void deleteProductType(Long id) {
        productTypeRepository.deleteById(id);
    }

    // ---- Warehouses ----

    @Transactional(readOnly = true)
    public List<WarehouseResponse> findAllWarehouses() {
        return warehouseRepository.findByCompanyId(TenantContext.getCompanyId())
                .stream().map(WarehouseResponse::from).toList();
    }

    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        Warehouse warehouse = Warehouse.builder()
                .customer(company.getCustomer())
                .company(company)
                .name(request.name())
                .code(request.code())
                .address(request.address())
                .phone(request.phone())
                .contactName(request.contactName())
                .active(request.isActive() == null || request.isActive())
                .build();
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    public void deleteWarehouse(Long id) {
        warehouseRepository.deleteById(id);
    }

    // ---- Inventory Movements ----

    @Transactional(readOnly = true)
    public PageResponse<InventoryMovementResponse> findAllMovements(Pageable pageable) {
        return PageResponse.from(
                movementRepository.findByCompanyId(TenantContext.getCompanyId(), pageable)
                        .map(InventoryMovementResponse::from)
        );
    }

    public InventoryMovementResponse createMovement(InventoryMovementRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.warehouseId()));

        InventoryMovement movement = InventoryMovement.builder()
                .customer(company.getCustomer())
                .company(company)
                .productId(request.productId())
                .warehouse(warehouse)
                .type(request.type())
                .quantity(request.quantity())
                .documentType(request.documentType() != null ? request.documentType() : "manual")
                .documentNumber(request.documentNumber())
                .notes(request.notes())
                .build();
        return InventoryMovementResponse.from(movementRepository.save(movement));
    }
}
