package com.coteja.erp.module.billing.repository;

import com.coteja.erp.module.billing.entity.BillingSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingSettingRepository extends JpaRepository<BillingSetting, Long> {

    List<BillingSetting> findByCompanyId(Long companyId);

    Optional<BillingSetting> findByCompanyIdAndKey(Long companyId, String key);
}
