package com.coteja.erp.module.billing.repository;

import com.coteja.erp.module.billing.entity.BillingClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingClientRepository extends JpaRepository<BillingClient, Long> {

    Page<BillingClient> findByCompanyId(Long companyId, Pageable pageable);

    List<BillingClient> findByCompanyId(Long companyId);

    Optional<BillingClient> findByCompanyIdAndClientsVariosTrue(Long companyId);

    @Query("SELECT c FROM BillingClient c WHERE c.company.id = :companyId " +
           "AND (LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR c.documentNumber LIKE CONCAT('%',:q,'%'))")
    List<BillingClient> search(Long companyId, String q);
}
