package com.coteja.erp.module.billing.repository;

import com.coteja.erp.module.billing.entity.BillingDteCorrelative;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingDteCorrelativeRepository extends JpaRepository<BillingDteCorrelative, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM BillingDteCorrelative c WHERE c.company.id = :companyId " +
           "AND c.documentType = :documentType AND c.year = :year")
    Optional<BillingDteCorrelative> findForUpdate(Long companyId, String documentType, Integer year);

    Optional<BillingDteCorrelative> findByCompanyIdAndDocumentTypeAndYear(
            Long companyId, String documentType, Integer year);
}
