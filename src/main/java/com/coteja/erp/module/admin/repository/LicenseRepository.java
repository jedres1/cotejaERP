package com.coteja.erp.module.admin.repository;

import com.coteja.erp.module.admin.entity.License;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByLicenseKey(String licenseKey);

    List<License> findByCompanyId(Long companyId);

    List<License> findByCompanyIdAndStatus(Long companyId, String status);

    Page<License> findAll(Pageable pageable);
}
