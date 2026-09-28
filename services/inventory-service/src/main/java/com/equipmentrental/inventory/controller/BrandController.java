package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateBrandRequest;
import com.equipmentrental.inventory.dto.request.UpdateBrandRequest;
import com.equipmentrental.inventory.dto.response.BrandResponse;
import com.equipmentrental.inventory.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService service;

    @PostMapping
    public ResponseEntity<BrandResponse> create(
            @Valid
            @RequestBody
            CreateBrandRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public List<BrandResponse> getAll(
            @RequestParam Long organizationId
    ) {
        return service.getAll(
                organizationId
        );
    }

    @GetMapping("/{id}")
    public BrandResponse getById(
            @PathVariable Long id,
            @RequestParam Long organizationId
    ) {

        return service.getById(
                id,
                organizationId
        );
    }

    @PutMapping("/{id}")
    public BrandResponse update(
            @PathVariable Long id,
            @RequestParam Long organizationId,
            @Valid
            @RequestBody
            UpdateBrandRequest request
    ) {

        return service.update(
                id,
                organizationId,
                request
        );
    }

    @PatchMapping("/{id}/active")
    public BrandResponse changeActive(
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