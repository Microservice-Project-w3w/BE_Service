package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.response.EquipmentReservationResponse;
import com.equipmentrental.inventory.enums.ReservationStatus;
import com.equipmentrental.inventory.service.EquipmentReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/reservations")
@RequiredArgsConstructor
public class EquipmentReservationController {

    private final EquipmentReservationQueryService service;

    @GetMapping
    public List<EquipmentReservationResponse> findAll(
            @RequestParam(required = false) Long rentalOrderId,
            @RequestParam(required = false) ReservationStatus status
    ) {
        return service.findAll(
                rentalOrderId,
                status
        );
    }

    @GetMapping("/{id}")
    public EquipmentReservationResponse findById(
            @PathVariable Long id
    ) {
        return service.findById(id);
    }
}