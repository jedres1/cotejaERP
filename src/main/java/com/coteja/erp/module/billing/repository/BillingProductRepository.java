package com.coteja.erp.module.billing.repository;

import com.coteja.erp.module.billing.entity.BillingProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingProductRepository extends JpaRepository<BillingProduct, Long> {

    Page<BillingProduct> findByCompanyId(Long companyId, Pageable pageable);

    Optional<BillingProduct> findByCompanyIdAndCode(Long companyId, String code);

    @Query("SELECT p FROM BillingProduct p WHERE p.company.id = :companyId " +
           "AND (LOWER(p.description) LIKE LOWER(CONCAT('%',:q,'%')) OR p.code LIKE CONCAT('%',:q,'%'))")
    List<BillingProduct> search(Long companyId, String q);
}
