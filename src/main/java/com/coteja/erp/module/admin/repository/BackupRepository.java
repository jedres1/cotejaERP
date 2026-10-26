package com.coteja.erp.module.admin.repository;

import com.coteja.erp.module.admin.entity.Backup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupRepository extends JpaRepository<Backup, Long> {

    List<Backup> findByCompanyIdOrderByUploadedAtDesc(Long companyId);

    List<Backup> findByLicenseId(Long licenseId);

    Page<Backup> findAll(Pageable pageable);
}
