package com.coteja.erp.module.admin.repository;

import com.coteja.erp.module.admin.entity.AppRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppReleaseRepository extends JpaRepository<AppRelease, Long> {

    Optional<AppRelease> findTopByPlatformAndActiveTrueOrderByCreatedAtDesc(String platform);
}
