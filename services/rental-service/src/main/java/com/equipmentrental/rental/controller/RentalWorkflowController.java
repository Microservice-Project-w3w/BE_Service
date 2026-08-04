package com.equipmentrental.rental.controller;

import com.equipmentrental.rental.dto.*;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.service.RentalWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class RentalWorkflowController {
    private final RentalWorkflowService s;

    public RentalWorkflowController(RentalWorkflowService s) {
        this.s = s;
    }

    @GetMapping("/availability")
    Map<String, Object> availability(@RequestParam Long equipmentTypeId, @RequestParam LocalDateTime startAt,
                                     @RequestParam LocalDateTime endAt, @RequestParam Integer quantity) {
        return s.availability(equipmentTypeId, startAt, endAt, quantity);
    }

    @GetMapping("/equipment/search")
    Map<String, Object> search(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long equipmentTypeId, @RequestParam(required = false) String brand,
                               @RequestParam(required = false) String status) {
        return Map.of("keyword", String.valueOf(keyword), "equipmentTypeId", String.valueOf(equipmentTypeId), "brand",
                String.valueOf(brand), "status", String.valueOf(status),
                "results", List.of(), "note", "Cần tích hợp inventory-service để trả thiết bị thật.");
    }

    @PostMapping("/rental-requests")
    ResponseEntity<RentalRequest> request(@Valid @RequestBody RentalRequestCreate r) {
        return ResponseEntity.status(201).body(s.createRequest(r));
    }

    @GetMapping("/rental-requests")
    List<RentalRequest> requests() {
        return s.getRequests();
    }

    @GetMapping("/rental-requests/{id}")
    RentalRequest request(@PathVariable Long id) {
        return s.getRequest(id);
    }

    @PatchMapping("/rental-requests/{id}/cancel")
    RentalRequest cancelRequest(@PathVariable Long id) {
        return s.cancelRequest(id);
    }

    @PostMapping("/quotations")
    ResponseEntity<Quotation> quotation(@Valid @RequestBody QuotationCreate r) {
        return ResponseEntity.status(201).body(s.createQuotation(r));
    }

    @GetMapping("/quotations")
    List<Quotation> quotations() {
        return s.getQuotations();
    }

    @PatchMapping("/quotations/{id}/send")
    Quotation send(@PathVariable Long id) {
        return s.sendQuotation(id);
    }

    @PatchMapping("/quotations/{id}/approve")
    Quotation approve(@PathVariable Long id) {
        return s.approveQuotation(id);
    }

    @PatchMapping("/quotations/{id}/accept")
    Quotation accept(@PathVariable Long id) {
        return s.acceptQuotation(id);
    }

    @PostMapping("/quotations/{id}/convert-to-order")
    RentalOrder convert(@PathVariable Long id) {
        return s.convertToOrder(id);
    }

    @GetMapping("/rental-orders")
    List<RentalOrder> orders() {
        return s.getOrders();
    }

    @PatchMapping("/rental-orders/{id}/reserve")
    RentalOrder reserve(@PathVariable Long id, @Valid @RequestBody ReserveOrderRequest r) {
        return s.reserve(id, r);
    }

    @PatchMapping("/rental-orders/{id}/cancel")
    RentalOrder cancelOrder(@PathVariable Long id, @Valid @RequestBody CancelOrderRequest r) {
        return s.cancelOrder(id, r);
    }
}
