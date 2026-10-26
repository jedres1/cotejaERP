package com.coteja.erp.module.admin.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.LicenseRequest;
import com.coteja.erp.module.admin.dto.LicenseResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.entity.*;
import com.coteja.erp.module.admin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final CompanyRepository companyRepository;
    private final PlanRepository planRepository;
    private final LicenseDeviceRepository licenseDeviceRepository;

    @Transactional(readOnly = true)
    public PageResponse<LicenseResponse> findAll(Pageable pageable) {
        return PageResponse.from(
                licenseRepository.findAll(pageable).map(LicenseResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public LicenseResponse findById(Long id) {
        return licenseRepository.findById(id)
                .map(LicenseResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("License", id));
    }

    @Transactional(readOnly = true)
    public List<LicenseResponse> findByCompanyId(Long companyId) {
        return licenseRepository.findByCompanyId(companyId)
                .stream().map(LicenseResponse::from).toList();
    }

    public LicenseResponse create(LicenseRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.companyId()));
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.planId()));

        License license = License.builder()
                .company(company)
                .plan(plan)
                .licenseKey(UUID.randomUUID().toString())
                .licenseType(request.licenseType() != null ? request.licenseType() : "propietario")
                .status(request.status() != null ? request.status() : "active")
                .startsAt(request.startsAt())
                .expiresAt(request.expiresAt())
                .maxUsers(request.maxUsers() != null ? request.maxUsers() : plan.getIncludedUsers())
                .maxDevices(request.maxDevices() != null ? request.maxDevices() : plan.getMaxDevices())
                .graceDays(request.graceDays() != null ? request.graceDays() : 0)
                .build();
        return LicenseResponse.from(licenseRepository.save(license));
    }

    public LicenseResponse update(Long id, LicenseRequest request) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License", id));
        if (request.status() != null) license.setStatus(request.status());
        if (request.licenseType() != null) license.setLicenseType(request.licenseType());
        if (request.startsAt() != null) license.setStartsAt(request.startsAt());
        if (request.expiresAt() != null) license.setExpiresAt(request.expiresAt());
        if (request.maxUsers() != null) license.setMaxUsers(request.maxUsers());
        if (request.maxDevices() != null) license.setMaxDevices(request.maxDevices());
        if (request.graceDays() != null) license.setGraceDays(request.graceDays());
        return LicenseResponse.from(licenseRepository.save(license));
    }

    public ApiResponse<LicenseResponse> validateLicense(String licenseKey, String deviceId) {
        License license = licenseRepository.findByLicenseKey(licenseKey)
                .orElse(null);

        if (license == null) {
            return ApiResponse.error("Licencia no encontrada");
        }
        if (!license.isActive()) {
            return ApiResponse.error("Licencia inactiva o expirada");
        }

        List<LicenseDevice> devices = licenseDeviceRepository.findByLicenseId(license.getId());
        boolean deviceRegistered = devices.stream()
                .anyMatch(d -> d.getDeviceId().equals(deviceId) && d.isActive());

        if (!deviceRegistered) {
            long activeDevices = devices.stream().filter(LicenseDevice::isActive).count();
            if (activeDevices >= license.getMaxDevices()) {
                return ApiResponse.error("Número máximo de dispositivos alcanzado");
            }
            LicenseDevice newDevice = LicenseDevice.builder()
                    .license(license)
                    .deviceId(deviceId)
                    .activatedAt(LocalDateTime.now())
                    .lastSeenAt(LocalDateTime.now())
                    .build();
            licenseDeviceRepository.save(newDevice);
        } else {
            licenseDeviceRepository.findByLicenseIdAndDeviceId(license.getId(), deviceId)
                    .ifPresent(d -> {
                        d.setLastSeenAt(LocalDateTime.now());
                        licenseDeviceRepository.save(d);
                    });
        }

        license.setLastValidatedAt(LocalDateTime.now());
        licenseRepository.save(license);

        return ApiResponse.ok("Licencia válida", LicenseResponse.from(license));
    }
}
