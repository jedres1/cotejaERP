package com.coteja.erp.module.banking.repository;

import com.coteja.erp.module.banking.entity.BankReconciliation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankReconciliationRepository extends JpaRepository<BankReconciliation, Long> {

    Page<BankReconciliation> findByBankAccountId(Long bankAccountId, Pageable pageable);

    Optional<BankReconciliation> findByBankAccountIdAndPeriodYearAndPeriodMonth(
            Long bankAccountId, Integer year, Integer month);
}
