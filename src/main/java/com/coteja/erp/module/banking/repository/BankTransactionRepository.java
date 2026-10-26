package com.coteja.erp.module.banking.repository;

import com.coteja.erp.module.banking.entity.BankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    Page<BankTransaction> findByBankAccountId(Long bankAccountId, Pageable pageable);

    Page<BankTransaction> findByBankAccountIdAndReconciledFalse(Long bankAccountId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'entrada' THEN t.amount ELSE -t.amount END), 0) " +
           "FROM BankTransaction t WHERE t.bankAccount.id = :accountId")
    BigDecimal calculateBalance(Long accountId);
}
