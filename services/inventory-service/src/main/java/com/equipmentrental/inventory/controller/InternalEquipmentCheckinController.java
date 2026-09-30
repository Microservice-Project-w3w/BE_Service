package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CheckinEquipmentRequest;
import com.equipmentrental.inventory.dto.response.CheckinEquipmentResponse;
import com.equipmentrental.inventory.service.InternalEquipmentCheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/equipment")
@RequiredArgsConstructor
public class InternalEquipmentCheckinController {

    private final InternalEquipmentCheckinService service;

    @PostMapping("/{id}/checkin")
    public CheckinEquipmentResponse checkin(
            @PathVariable Long id,
            @RequestBody CheckinEquipmentRequest request
    ) {

        return service.checkin(
                id,
                request
        );
    }
}