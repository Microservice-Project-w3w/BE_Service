package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.ReservationResponse;
import com.equipmentrental.inventory.dto.ReserveEquipmentRequest;
import com.equipmentrental.inventory.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/inventory/reservations")
public class InternalReservationController {

    private final ReservationService reservationService;

    public InternalReservationController(
            ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody ReserveEquipmentRequest request
    ) {
        return ResponseEntity.ok(
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
}