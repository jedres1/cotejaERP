package com.coteja.erp.module.purchases.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.purchases.dto.PurchaseInvoiceRequest;
import com.coteja.erp.module.purchases.dto.PurchaseInvoiceResponse;
import com.coteja.erp.module.purchases.entity.PurchaseInvoice;
import com.coteja.erp.module.purchases.entity.Supplier;
import com.coteja.erp.module.purchases.repository.PurchaseInvoiceRepository;
import com.coteja.erp.module.purchases.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseInvoiceService {

    private final PurchaseInvoiceRepository invoiceRepository;
    private final SupplierRepository supplierRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public PageResponse<PurchaseInvoiceResponse> findAll(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return PageResponse.from(
                invoiceRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                        .map(PurchaseInvoiceResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceResponse findById(Long id) {
        return invoiceRepository.findById(id)
                .map(PurchaseInvoiceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseInvoiceResponse> findAccountsPayable(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return PageResponse.from(
                invoiceRepository.findAccountsPayable(companyId, pageable)
                        .map(PurchaseInvoiceResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public List<PurchaseInvoiceResponse> findPendingApproval() {
        Long companyId = TenantContext.getCompanyId();
        return invoiceRepository.findPendingApproval(companyId)
                .stream().map(PurchaseInvoiceResponse::from).toList();
    }

    public PurchaseInvoiceResponse create(PurchaseInvoiceRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        Supplier supplier = null;
        if (request.supplierId() != null) {
            supplier = supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));
        }

        BigDecimal iva = request.iva() != null ? request.iva() : BigDecimal.ZERO;
        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .customer(company.getCustomer())
                .company(company)
                .supplier(supplier)
                .documentType(request.documentType() != null ? request.documentType() : "03")
                .invoiceNumber(request.invoiceNumber())
                .purchaseDate(request.purchaseDate())
                .dueDate(request.dueDate())
                .subtotal(request.subtotal())
                .iva(iva)
                .total(request.total())
                .paymentMethod(request.paymentMethod())
                .notes(request.notes())
                .build();
        return PurchaseInvoiceResponse.from(invoiceRepository.save(invoice));
    }

    public PurchaseInvoiceResponse update(Long id, PurchaseInvoiceRequest request) {
        PurchaseInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", id));
        if (request.supplierId() != null) {
            supplierRepository.findById(request.supplierId())
                    .ifPresent(invoice::setSupplier);
        }
        invoice.setInvoiceNumber(request.invoiceNumber());
        invoice.setPurchaseDate(request.purchaseDate());
        invoice.setDueDate(request.dueDate());
        invoice.setSubtotal(request.subtotal());
        invoice.setIva(request.iva() != null ? request.iva() : BigDecimal.ZERO);
        invoice.setTotal(request.total());
        invoice.setPaymentMethod(request.paymentMethod());
        invoice.setNotes(request.notes());
        return PurchaseInvoiceResponse.from(invoiceRepository.save(invoice));
    }

    public void approve(Long id) {
        changeStatus(id, "approved");
    }

    public void reject(Long id) {
        changeStatus(id, "rejected");
    }

    public void approveBulk(List<Long> ids) {
        ids.forEach(this::approve);
    }

    public void rejectBulk(List<Long> ids) {
        ids.forEach(this::reject);
    }

    public void markPaid(Long id) {
        PurchaseInvoice invoice = getOrThrow(id);
        invoice.setPaymentStatus("paid");
        invoiceRepository.save(invoice);
    }

    public void softDelete(Long id) {
        PurchaseInvoice invoice = getOrThrow(id);
        invoice.setDeletedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    private void changeStatus(Long id, String status) {
        PurchaseInvoice invoice = getOrThrow(id);
        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }

    private PurchaseInvoice getOrThrow(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", id));
    }
}
