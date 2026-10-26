package com.coteja.erp.module.accounting.repository;

import com.coteja.erp.module.accounting.entity.AccountingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, Long> {

    List<AccountingPeriod> findByCompanyIdOrderByYearDescMonthDesc(Long companyId);

    Optional<AccountingPeriod> findByCompanyIdAndYearAndMonth(Long companyId, Integer year, Integer month);

    List<AccountingPeriod> findByCompanyIdAndYear(Long companyId, Integer year);
}
