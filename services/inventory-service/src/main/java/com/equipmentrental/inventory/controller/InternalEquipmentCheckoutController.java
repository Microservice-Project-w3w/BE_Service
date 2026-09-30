package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CheckoutEquipmentRequest;
import com.equipmentrental.inventory.dto.response.CheckoutEquipmentResponse;
import com.equipmentrental.inventory.service.InternalEquipmentCheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/equipment")
@RequiredArgsConstructor
public class InternalEquipmentCheckoutController {

    private final InternalEquipmentCheckoutService service;

    @PostMapping("/{id}/checkout")
    public CheckoutEquipmentResponse checkout(
            @PathVariable Long id,
            @RequestBody CheckoutEquipmentRequest request
    ) {

        return service.checkout(
                id,
                request
        );
    }
}