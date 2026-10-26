package com.coteja.erp.module.billing.repository;

import com.coteja.erp.module.billing.entity.BillingInvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingInvoicePaymentRepository extends JpaRepository<BillingInvoicePayment, Long> {

    List<BillingInvoicePayment> findByInvoiceId(Long invoiceId);
}
