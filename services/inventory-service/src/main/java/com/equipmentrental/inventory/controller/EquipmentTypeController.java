package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateEquipmentTypeRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentTypeRequest;
import com.equipmentrental.inventory.dto.response.EquipmentTypeResponse;
import com.equipmentrental.inventory.service.EquipmentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/equipment-types")
@RequiredArgsConstructor
public class EquipmentTypeController {

    private final EquipmentTypeService service;

    @PostMapping
    public ResponseEntity<EquipmentTypeResponse> create(
            @Valid
            @RequestBody
            CreateEquipmentTypeRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public List<EquipmentTypeResponse> getAll(
            @RequestParam Long organizationId,

            @RequestParam(required = false)
            Long categoryId
    ) {

        return service.getAll(
                organizationId,
                categoryId
        );
    }

    @GetMapping("/{id}")
    public EquipmentTypeResponse getById(
            @PathVariable Long id,

            @RequestParam Long organizationId
    ) {

        return service.getById(
                id,
                organizationId
        );
    }

    @PutMapping("/{id}")
    public EquipmentTypeResponse update(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @Valid
            @RequestBody
            UpdateEquipmentTypeRequest request
    ) {

        return service.update(
                id,
                organizationId,
                request
        );
    }

    @PatchMapping("/{id}/active")
    public EquipmentTypeResponse changeActive(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @RequestParam boolean active
    ) {

        return service.changeActive(
                id,
                organizationId,
                active
        );
    }
}