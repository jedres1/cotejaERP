package com.coteja.erp.module.admin.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.admin.dto.AppReleaseRequest;
import com.coteja.erp.module.admin.dto.AppReleaseResponse;
import com.coteja.erp.module.admin.entity.AppRelease;
import com.coteja.erp.module.admin.repository.AppReleaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppReleaseService {

    private final AppReleaseRepository releaseRepository;

    @Transactional(readOnly = true)
    public List<AppReleaseResponse> findAll() {
        return releaseRepository.findAll().stream().map(AppReleaseResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AppReleaseResponse findById(Long id) {
        return releaseRepository.findById(id)
                .map(AppReleaseResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("AppRelease", id));
    }

    @Transactional(readOnly = true)
    public AppReleaseResponse findLatest(String platform) {
        return releaseRepository.findTopByPlatformAndActiveTrueOrderByCreatedAtDesc(platform)
                .map(AppReleaseResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No active release found for platform: " + platform));
    }

    public AppReleaseResponse create(AppReleaseRequest request) {
        AppRelease release = AppRelease.builder()
                .platform(request.platform())
                .version(request.version())
                .filename(request.filename())
                .downloadUrl(request.downloadUrl())
                .notes(request.notes())
                .active(request.isActive() == null || request.isActive())
                .build();
        return AppReleaseResponse.from(releaseRepository.save(release));
    }

    public AppReleaseResponse update(Long id, AppReleaseRequest request) {
        AppRelease release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppRelease", id));
        release.setPlatform(request.platform());
        release.setVersion(request.version());
        release.setFilename(request.filename());
        release.setDownloadUrl(request.downloadUrl());
        release.setNotes(request.notes());
        if (request.isActive() != null) release.setActive(request.isActive());
        return AppReleaseResponse.from(releaseRepository.save(release));
    }

    public void delete(Long id) {
        if (!releaseRepository.existsById(id)) throw new ResourceNotFoundException("AppRelease", id);
        releaseRepository.deleteById(id);
    }
}
