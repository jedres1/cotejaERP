package com.coteja.erp.module.admin.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.module.admin.dto.PlanRequest;
import com.coteja.erp.module.admin.dto.PlanResponse;
import com.coteja.erp.module.admin.entity.Plan;
import com.coteja.erp.module.admin.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanResponse> findAll() {
        return planRepository.findAll().stream().map(PlanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse findById(Long id) {
        return planRepository.findById(id)
                .map(PlanResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", id));
    }

    public PlanResponse create(PlanRequest request) {
        Plan plan = Plan.builder()
                .code(request.code())
                .name(request.name())
                .monthlyPrice(request.monthlyPrice())
                .annualPrice(request.annualPrice())
                .implementationFee(request.implementationFee())
                .additionalUserPrice(request.additionalUserPrice())
                .includedUsers(request.includedUsers() != null ? request.includedUsers() : 1)
                .maxDevices(request.maxDevices() != null ? request.maxDevices() : 1)
                .supportIncluded(Boolean.TRUE.equals(request.supportIncluded()))
                .cloudBackup(Boolean.TRUE.equals(request.cloudBackup()))
                .unlimitedDocuments(Boolean.TRUE.equals(request.unlimitedDocuments()))
                .active(request.isActive() == null || request.isActive())
                .build();
        return PlanResponse.from(planRepository.save(plan));
    }

    public PlanResponse update(Long id, PlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", id));
        plan.setCode(request.code());
        plan.setName(request.name());
        plan.setMonthlyPrice(request.monthlyPrice());
        plan.setAnnualPrice(request.annualPrice());
        if (request.implementationFee() != null) plan.setImplementationFee(request.implementationFee());
        if (request.additionalUserPrice() != null) plan.setAdditionalUserPrice(request.additionalUserPrice());
        if (request.includedUsers() != null) plan.setIncludedUsers(request.includedUsers());
        if (request.maxDevices() != null) plan.setMaxDevices(request.maxDevices());
        if (request.supportIncluded() != null) plan.setSupportIncluded(request.supportIncluded());
        if (request.cloudBackup() != null) plan.setCloudBackup(request.cloudBackup());
        if (request.unlimitedDocuments() != null) plan.setUnlimitedDocuments(request.unlimitedDocuments());
        if (request.isActive() != null) plan.setActive(request.isActive());
        return PlanResponse.from(planRepository.save(plan));
    }

    public void delete(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", id));
        planRepository.delete(plan);
    }
}
