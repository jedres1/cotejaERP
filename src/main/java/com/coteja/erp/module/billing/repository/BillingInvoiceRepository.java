package com.coteja.erp.module.billing.repository;

import com.coteja.erp.module.billing.entity.BillingInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, Long> {

    Page<BillingInvoice> findByCompanyIdAndDeletedAtIsNull(Long companyId, Pageable pageable);

    Optional<BillingInvoice> findByGenerationCode(String generationCode);

    /** Cuentas por cobrar: facturas aceptadas con saldo pendiente */
    @Query("SELECT i FROM BillingInvoice i WHERE i.company.id = :companyId " +
           "AND i.paymentStatus IN ('pending','partial') AND i.accepted = true " +
           "AND i.deletedAt IS NULL ORDER BY i.issuedAt ASC")
    List<BillingInvoice> findAccountsReceivable(Long companyId);

    @Query("SELECT i FROM BillingInvoice i WHERE i.company.id = :companyId " +
           "AND i.status = :status AND i.deletedAt IS NULL")
    Page<BillingInvoice> findByStatus(Long companyId, String status, Pageable pageable);
}
