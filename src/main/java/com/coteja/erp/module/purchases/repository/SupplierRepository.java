package com.coteja.erp.module.purchases.repository;

import com.coteja.erp.module.purchases.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Page<Supplier> findByCompanyIdAndDeletedAtIsNull(Long companyId, Pageable pageable);

    List<Supplier> findByCompanyIdAndDeletedAtIsNull(Long companyId);

    boolean existsByCompanyIdAndDocumentNumber(Long companyId, String documentNumber);
}
