package com.coteja.erp.module.admin.repository;

import com.coteja.erp.module.admin.entity.LicenseDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseDeviceRepository extends JpaRepository<LicenseDevice, Long> {

    List<LicenseDevice> findByLicenseId(Long licenseId);

    Optional<LicenseDevice> findByLicenseIdAndDeviceId(Long licenseId, String deviceId);
}
