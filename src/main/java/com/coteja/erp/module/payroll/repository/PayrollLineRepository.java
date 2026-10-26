package com.coteja.erp.module.payroll.repository;

import com.coteja.erp.module.payroll.entity.PayrollLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollLineRepository extends JpaRepository<PayrollLine, Long> {

    List<PayrollLine> findByPayrollPeriodId(Long periodId);
}
