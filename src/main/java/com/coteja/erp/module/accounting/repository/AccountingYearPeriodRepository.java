package com.coteja.erp.module.accounting.repository;

import com.coteja.erp.module.accounting.entity.AccountingYearPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingYearPeriodRepository extends JpaRepository<AccountingYearPeriod, Long> {

    List<AccountingYearPeriod> findByCompanyIdOrderByYearDesc(Long companyId);

    Optional<AccountingYearPeriod> findByCompanyIdAndYear(Long companyId, Integer year);
}
