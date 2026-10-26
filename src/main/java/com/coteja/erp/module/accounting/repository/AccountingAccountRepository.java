package com.coteja.erp.module.accounting.repository;

import com.coteja.erp.module.accounting.entity.AccountingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingAccountRepository extends JpaRepository<AccountingAccount, Long> {

    List<AccountingAccount> findByCompanyIdOrderByCode(Long companyId);

    List<AccountingAccount> findByCompanyIdAndActiveTrueOrderByCode(Long companyId);

    @Query("SELECT a FROM AccountingAccount a WHERE a.company.id = :companyId AND a.children IS EMPTY AND a.active = true ORDER BY a.code")
    List<AccountingAccount> findLeafAccounts(Long companyId);

    boolean existsByCompanyId(Long companyId);

    Optional<AccountingAccount> findByCompanyIdAndCode(Long companyId, String code);
}
