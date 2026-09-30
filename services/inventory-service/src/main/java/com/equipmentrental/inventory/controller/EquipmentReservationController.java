package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.ReservationResponse;
import com.equipmentrental.inventory.dto.ReserveEquipmentRequest;
import com.equipmentrental.inventory.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/reservations")
public class EquipmentReservationController {

    private final ReservationService reservationService;

    public EquipmentReservationController(
            ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody ReserveEquipmentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        reservationService.reserve(request)
                );
    }

    @PostMapping("/{reservationId}/confirm")
    public ResponseEntity<ReservationResponse> confirm(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                reservationService.confirm(reservationId)
        );
    }

    @PostMapping("/{reservationId}/release")
    public ResponseEntity<ReservationResponse> release(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                reservationService.release(reservationId)
        );
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> get(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                reservationService.get(reservationId)
        );
    }
}