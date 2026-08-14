package com.equipmentrental.logistics.service;

import com.equipmentrental.logistics.dto.request.CreateDeliveryFeeRuleRequest;
import com.equipmentrental.logistics.dto.response.DeliveryFeeRuleResponse;
import com.equipmentrental.logistics.entity.DeliveryFeeRule;
import com.equipmentrental.logistics.repository.DeliveryFeeRuleRepository;
import com.equipmentrental.logistics.exception.ResourceNotFoundException;
import com.equipmentrental.logistics.security.LogisticsDataScopeGuard;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryFeeRuleService {

    private final DeliveryFeeRuleRepository repository;
    private final LogisticsDataScopeGuard dataScopeGuard;

    public DeliveryFeeRuleService(DeliveryFeeRuleRepository repository, LogisticsDataScopeGuard dataScopeGuard) {
        this.repository = repository;
        this.dataScopeGuard = dataScopeGuard;
    }

    @Transactional
    public DeliveryFeeRuleResponse createRule(CreateDeliveryFeeRuleRequest request) {

        dataScopeGuard.requireOrganizationOrBranch(request.getOrganizationId(), request.getBranchId());

        DeliveryFeeRule rule = new DeliveryFeeRule();

        rule.setOrganizationId(request.getOrganizationId());
        rule.setBranchId(request.getBranchId());

        rule.setName(request.getName());
        rule.setBaseFee(request.getBaseFee());
        rule.setMaxDistanceKm(request.getMaxDistanceKm());
        rule.setExtraFeePerKm(request.getExtraFeePerKm());
        rule.setIsActive(request.getIsActive());

        DeliveryFeeRule savedRule = repository.save(rule);

        return mapToResponse(savedRule);
    }

    @Transactional(readOnly = true)
    public List<DeliveryFeeRuleResponse> getActiveRules() {
        return repository.findByIsActiveTrue().stream()
                .filter(rule -> dataScopeGuard.canAccessOrganizationOrBranch(
                        rule.getOrganizationId(), rule.getBranchId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DeliveryFeeRuleResponse mapToResponse(DeliveryFeeRule rule) {

        DeliveryFeeRuleResponse res = new DeliveryFeeRuleResponse();

        res.setId(rule.getId());
        res.setName(rule.getName());
        res.setBaseFee(rule.getBaseFee());
        res.setMaxDistanceKm(rule.getMaxDistanceKm());
        res.setExtraFeePerKm(rule.getExtraFeePerKm());
        res.setIsActive(rule.getIsActive());

        return res;
    }

    @Transactional(readOnly = true)
    public DeliveryFeeRuleResponse getById(Long id) {

        DeliveryFeeRule rule = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Delivery fee rule not found"
                        )
                );

        dataScopeGuard.requireOrganizationOrBranch(rule.getOrganizationId(), rule.getBranchId());

        return mapToResponse(rule);
    }

    @Transactional
    public DeliveryFeeRuleResponse update(
            Long id,
            CreateDeliveryFeeRuleRequest request
    ) {

        DeliveryFeeRule rule = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Delivery fee rule not found"
                        )
                );

        dataScopeGuard.requireOrganizationOrBranch(rule.getOrganizationId(), rule.getBranchId());
        dataScopeGuard.requireOrganizationOrBranch(request.getOrganizationId(), request.getBranchId());

        rule.setOrganizationId(request.getOrganizationId());
        rule.setBranchId(request.getBranchId());
        rule.setName(request.getName());
        rule.setBaseFee(request.getBaseFee());
        rule.setMaxDistanceKm(request.getMaxDistanceKm());
        rule.setExtraFeePerKm(request.getExtraFeePerKm());
        rule.setIsActive(request.getIsActive());

        return mapToResponse(repository.save(rule));
    }

    @Transactional
    public DeliveryFeeRuleResponse updateActive(
            Long id,
            boolean active
    ) {

        DeliveryFeeRule rule = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Delivery fee rule not found"
                        )
                );

        dataScopeGuard.requireOrganizationOrBranch(rule.getOrganizationId(), rule.getBranchId());

        rule.setIsActive(active);

        return mapToResponse(repository.save(rule));
    }
}
