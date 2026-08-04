package com.equipmentrental.rental.controller;

import com.equipmentrental.rental.dto.RentalPriceRequest;
import com.equipmentrental.rental.dto.RentalPriceResponse;
import com.equipmentrental.rental.service.RentalPriceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rental-prices")
public class RentalPriceController {

    private final RentalPriceService rentalPriceService;

    public RentalPriceController(RentalPriceService rentalPriceService) {
        this.rentalPriceService = rentalPriceService;
    }

    @PostMapping
    public ResponseEntity<RentalPriceResponse> create(
            @Valid @RequestBody RentalPriceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(rentalPriceService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RentalPriceResponse>> getAll(
            @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(
                rentalPriceService.getAll(active)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalPriceResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                rentalPriceService.getById(id)
        );
    }

    @GetMapping("/equipment-types/{equipmentTypeId}")
    public ResponseEntity<List<RentalPriceResponse>> getByEquipmentType(
            @PathVariable Long equipmentTypeId
    ) {
        return ResponseEntity.ok(
                rentalPriceService.getByEquipmentTypeId(equipmentTypeId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RentalPriceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RentalPriceRequest request
    ) {
        return ResponseEntity.ok(
                rentalPriceService.update(id, request)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RentalPriceResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(
                rentalPriceService.updateStatus(id, active)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        rentalPriceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}