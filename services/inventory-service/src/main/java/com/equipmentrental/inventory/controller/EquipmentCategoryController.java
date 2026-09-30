package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateEquipmentCategoryRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentCategoryRequest;
import com.equipmentrental.inventory.dto.response.EquipmentCategoryResponse;
import com.equipmentrental.inventory.service.EquipmentCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/categories")
@RequiredArgsConstructor
public class EquipmentCategoryController {

    private final EquipmentCategoryService service;


    @PostMapping
    public ResponseEntity<EquipmentCategoryResponse> create(
            @Valid @RequestBody CreateEquipmentCategoryRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public List<EquipmentCategoryResponse> getAll(
            @RequestParam Long organizationId
    ) {
        return service.getAll(organizationId);
    }

    @GetMapping("/{id}")
    public EquipmentCategoryResponse getById(
            @PathVariable Long id,
            @RequestParam Long organizationId
    ) {
        return service.getById(id, organizationId);
    }

    @PutMapping("/{id}")
    public EquipmentCategoryResponse update(
            @PathVariable Long id,
            @RequestParam Long organizationId,
            @Valid @RequestBody UpdateEquipmentCategoryRequest request
    ) {
        return service.update(
                id,
                organizationId,
                request
        );
    }


    @PatchMapping("/{id}/active")
    public EquipmentCategoryResponse changeActive(
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