package com.coteja.erp.module.purchases.repository;

import com.coteja.erp.module.purchases.entity.PurchaseInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {

    Page<PurchaseInvoice> findByCompanyIdAndDeletedAtIsNull(Long companyId, Pageable pageable);

    Page<PurchaseInvoice> findByCompanyIdAndStatusAndDeletedAtIsNull(Long companyId, String status, Pageable pageable);

    Page<PurchaseInvoice> findByCompanyIdAndPaymentStatusAndDeletedAtIsNull(Long companyId, String paymentStatus, Pageable pageable);

    @Query("SELECT p FROM PurchaseInvoice p WHERE p.company.id = :companyId AND p.status = 'registered' AND p.deletedAt IS NULL")
    List<PurchaseInvoice> findPendingApproval(Long companyId);

    @Query("SELECT p FROM PurchaseInvoice p WHERE p.company.id = :companyId AND p.paymentStatus != 'paid' AND p.deletedAt IS NULL")
    Page<PurchaseInvoice> findAccountsPayable(Long companyId, Pageable pageable);

    Optional<PurchaseInvoice> findByCompanyIdAndDocumentTypeAndInvoiceNumber(
            Long companyId, String documentType, String invoiceNumber);
}
