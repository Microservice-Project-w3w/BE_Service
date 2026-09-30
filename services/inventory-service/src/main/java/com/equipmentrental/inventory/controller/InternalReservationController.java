package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.ConfirmInternalReservationRequest;
import com.equipmentrental.inventory.dto.request.CreateInternalReservationRequest;
import com.equipmentrental.inventory.dto.response.EquipmentReservationResponse;
import com.equipmentrental.inventory.service.InternalReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.equipmentrental.inventory.dto.request.ReleaseInternalReservationRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/reservations")
@RequiredArgsConstructor
public class InternalReservationController {

    private final InternalReservationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentReservationResponse create(
            @RequestBody
            CreateInternalReservationRequest request
    ) {

        return service.create(request);
    }

    @PostMapping("/{id}/confirm")
    public EquipmentReservationResponse confirm(
            @PathVariable Long id,
            @RequestBody(required = false)
            ConfirmInternalReservationRequest request
    ) {

        return service.confirm(
                id,
                request
        );
    }
    @PostMapping("/{id}/release")
    public EquipmentReservationResponse release(
            @PathVariable Long id,
            @RequestBody ReleaseInternalReservationRequest request
    ) {

        return service.release(
                id,
                request
        );
    }
}