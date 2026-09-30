package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.AvailabilityRequest;
import com.equipmentrental.inventory.dto.AvailabilityResponse;
import com.equipmentrental.inventory.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(
            AvailabilityService availabilityService
    ) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/check")
    public ResponseEntity<AvailabilityResponse> check(
            @Valid @RequestBody AvailabilityRequest request
    ) {
        return ResponseEntity.ok(
                availabilityService.checkAvailability(request)
        );
    }
}