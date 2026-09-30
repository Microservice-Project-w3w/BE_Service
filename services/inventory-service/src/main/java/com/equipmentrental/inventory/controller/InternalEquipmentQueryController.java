package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.response.InternalEquipmentResponse;
import com.equipmentrental.inventory.service.InternalEquipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/equipment")
@RequiredArgsConstructor
public class InternalEquipmentQueryController {

    private final InternalEquipmentQueryService service;

    @GetMapping("/{id}")
    public InternalEquipmentResponse findById(
            @PathVariable Long id
    ) {

        return service.findById(id);
    }
}