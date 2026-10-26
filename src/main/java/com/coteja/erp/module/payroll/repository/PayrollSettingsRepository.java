package com.coteja.erp.module.payroll.repository;

import com.coteja.erp.module.payroll.entity.PayrollSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollSettingsRepository extends JpaRepository<PayrollSettings, Long> {

    Optional<PayrollSettings> findByCompanyId(Long companyId);
}
