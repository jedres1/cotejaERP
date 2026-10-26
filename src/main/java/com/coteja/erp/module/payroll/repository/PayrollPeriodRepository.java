package com.coteja.erp.module.payroll.repository;

import com.coteja.erp.module.payroll.entity.PayrollPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {

    Page<PayrollPeriod> findByCompanyIdAndDeletedAtIsNull(Long companyId, Pageable pageable);
}
