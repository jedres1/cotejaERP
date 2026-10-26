package com.coteja.erp.module.accounting.repository;

import com.coteja.erp.module.accounting.entity.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, Long> {

    List<CostCenter> findByCompanyIdAndActiveTrueOrderByCode(Long companyId);

    List<CostCenter> findByCompanyId(Long companyId);

    boolean existsByCompanyId(Long companyId);
}
