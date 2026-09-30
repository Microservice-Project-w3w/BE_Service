package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateEquipmentModelRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentModelRequest;
import com.equipmentrental.inventory.dto.response.EquipmentModelResponse;
import com.equipmentrental.inventory.service.EquipmentModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/models")
@RequiredArgsConstructor
public class EquipmentModelController {

    private final EquipmentModelService service;

    @PostMapping
    public ResponseEntity<EquipmentModelResponse> create(
            @Valid
            @RequestBody
            CreateEquipmentModelRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public List<EquipmentModelResponse> getAll(
            @RequestParam Long organizationId,

            @RequestParam(required = false)
            Long equipmentTypeId,

            @RequestParam(required = false)
            Long brandId
    ) {

        return service.getAll(
                organizationId,
                equipmentTypeId,
                brandId
        );
    }

    @GetMapping("/{id}")
    public EquipmentModelResponse getById(
            @PathVariable Long id,

            @RequestParam Long organizationId
    ) {

        return service.getById(
                id,
                organizationId
        );
    }

    @PutMapping("/{id}")
    public EquipmentModelResponse update(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @Valid
            @RequestBody
            UpdateEquipmentModelRequest request
    ) {

        return service.update(
                id,
                organizationId,
                request
        );
    }

    @PatchMapping("/{id}/active")
    public EquipmentModelResponse changeActive(
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