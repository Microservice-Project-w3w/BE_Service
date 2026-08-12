package com.equipmentrental.logistics.service;

import com.equipmentrental.logistics.dto.request.CreateDeliveryFeeRuleRequest;
import com.equipmentrental.logistics.dto.response.DeliveryFeeRuleResponse;
import com.equipmentrental.logistics.entity.DeliveryFeeRule;
import com.equipmentrental.logistics.repository.DeliveryFeeRuleRepository;
import com.equipmentrental.logistics.exception.ResourceNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryFeeRuleService {

    private final DeliveryFeeRuleRepository repository;

    public DeliveryFeeRuleService(DeliveryFeeRuleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DeliveryFeeRuleResponse createRule(CreateDeliveryFeeRuleRequest request) {

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
        return repository.findByIsActiveTrue().stream().map(this::mapToResponse).collect(Collectors.toList());
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

        rule.setIsActive(active);

        return mapToResponse(repository.save(rule));
    }
}
