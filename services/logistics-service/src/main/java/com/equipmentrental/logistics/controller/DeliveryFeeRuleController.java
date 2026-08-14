package com.equipmentrental.logistics.controller;

import com.equipmentrental.logistics.dto.request.CreateDeliveryFeeRuleRequest;
import com.equipmentrental.logistics.dto.response.DeliveryFeeRuleResponse;
import com.equipmentrental.logistics.service.DeliveryFeeRuleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/logistics/delivery-fee-rules")
public class DeliveryFeeRuleController {
    private final DeliveryFeeRuleService service;

    public DeliveryFeeRuleController(DeliveryFeeRuleService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('logistics.delivery-fee.manage')")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryFeeRuleResponse createRule(@Valid @RequestBody CreateDeliveryFeeRuleRequest request) {
        return service.createRule(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('logistics.delivery-fee.read')")
    public List<DeliveryFeeRuleResponse> getActiveRules() {
        return service.getActiveRules();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('logistics.delivery-fee.read')")
    public DeliveryFeeRuleResponse getById(
            @PathVariable Long id
    ) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('logistics.delivery-fee.manage')")
    public DeliveryFeeRuleResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CreateDeliveryFeeRuleRequest request
    ) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAuthority('logistics.delivery-fee.manage')")
    public DeliveryFeeRuleResponse updateActive(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        return service.updateActive(id, active);
    }
}
