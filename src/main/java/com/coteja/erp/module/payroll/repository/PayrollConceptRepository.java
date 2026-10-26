package com.coteja.erp.module.payroll.repository;

import com.coteja.erp.module.payroll.entity.PayrollConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollConceptRepository extends JpaRepository<PayrollConcept, Long> {

    List<PayrollConcept> findByCompanyIdAndActiveTrue(Long companyId);

    List<PayrollConcept> findByCompanyIdAndAppliesToAllTrueAndActiveTrue(Long companyId);
}
