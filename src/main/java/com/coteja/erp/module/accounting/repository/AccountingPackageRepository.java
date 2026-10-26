package com.coteja.erp.module.accounting.repository;

import com.coteja.erp.module.accounting.entity.AccountingPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingPackageRepository extends JpaRepository<AccountingPackage, Long> {

    List<AccountingPackage> findByCompanyId(Long companyId);

    List<AccountingPackage> findByCompanyIdAndActiveTrue(Long companyId);

    Optional<AccountingPackage> findByCompanyIdAndCode(Long companyId, String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM AccountingPackage p WHERE p.id = :id")
    Optional<AccountingPackage> findByIdForUpdate(Long id);
}
