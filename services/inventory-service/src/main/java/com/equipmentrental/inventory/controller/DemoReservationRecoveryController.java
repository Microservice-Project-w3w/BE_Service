package com.equipmentrental.inventory.controller;
import com.equipmentrental.inventory.service.DemoReservationRecoveryService;
import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/internal/demo-reservations")
@PreAuthorize("hasAnyAuthority('inventory.reservation.create','inventory.reservation.confirm','inventory.reservation.release','rental.contract.update','rental.contract.sign')")
public class DemoReservationRecoveryController {
    private final DemoReservationRecoveryService service;
    public DemoReservationRecoveryController(DemoReservationRecoveryService service) { this.service=service; }
    @GetMapping("/{id}") public EquipmentReservation snapshot(@PathVariable Long id) { return service.snapshot(id); }
    @PostMapping("/compensate") public void compensate(@RequestBody JsonNode input) { service.compensate(input); }
}
