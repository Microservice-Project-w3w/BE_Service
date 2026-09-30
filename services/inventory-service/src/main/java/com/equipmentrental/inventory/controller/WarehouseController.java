package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateWarehouseRequest;
import com.equipmentrental.inventory.dto.request.UpdateWarehouseRequest;
import com.equipmentrental.inventory.dto.response.WarehouseResponse;
import com.equipmentrental.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    @PostMapping
    public WarehouseResponse create(
            @RequestBody CreateWarehouseRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    public List<WarehouseResponse> findAll(
            @RequestParam(required = false)
            Long organizationId,

            @RequestParam(required = false)
            Long branchId
    ) {
        return service.findAll(
                organizationId,
                branchId
        );
    }

    @GetMapping("/{id}")
    public WarehouseResponse findById(
            @PathVariable Long id
    ) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public WarehouseResponse update(
            @PathVariable Long id,
            @RequestBody UpdateWarehouseRequest request
    ) {
        return service.update(
                id,
                request
        );
    }

    @PatchMapping("/{id}/active")
    public WarehouseResponse changeActive(
            @PathVariable Long id,

            @RequestParam boolean active
    ) {
        return service.changeActive(
                id,
                active
        );
    }
}