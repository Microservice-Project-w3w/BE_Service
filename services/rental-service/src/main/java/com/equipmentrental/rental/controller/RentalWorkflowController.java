package com.equipmentrental.rental.controller;

import com.equipmentrental.common.web.ApiResponse;
import com.equipmentrental.rental.dto.QuotationCreate;
import com.equipmentrental.rental.dto.request.CancelOrderRequest;
import com.equipmentrental.rental.dto.request.QuotationUpdate;
import com.equipmentrental.rental.dto.request.RentalRequestCreate;
import com.equipmentrental.rental.dto.request.RentalRequestUpdate;
import com.equipmentrental.rental.dto.request.ReserveOrderRequest;
import com.equipmentrental.rental.dto.response.QuotationResponse;
import com.equipmentrental.rental.dto.response.RentalOrderResponse;
import com.equipmentrental.rental.dto.response.RentalRequestResponse;
import com.equipmentrental.rental.service.RentalWorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RentalWorkflowController {
    private final RentalWorkflowService s;

    public RentalWorkflowController(RentalWorkflowService s) {
        this.s = s;
    }

    @GetMapping("/availability")
    @PreAuthorize("hasAuthority('inventory.availability.read')")
    ApiResponse<JsonNode> availability(
            @RequestParam Long organizationId,
            @RequestParam Long branchId,
            @RequestParam Long equipmentTypeId,
            @RequestParam LocalDateTime startAt,
            @RequestParam LocalDateTime endAt,
            @RequestParam Integer quantity) {
        return ApiResponse.success(s.availability(organizationId, branchId, equipmentTypeId, startAt, endAt, quantity));
    }

    @GetMapping("/equipment/search")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    ApiResponse<Map<String, Object>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("keyword", keyword);
        response.put("equipmentTypeId", equipmentTypeId);
        response.put("brand", brand);
        response.put("status", status);
        response.put("results", List.of());
        response.put("note", "Cần tích hợp inventory-service để trả thiết bị thật.");
        return ApiResponse.success(response);
    }

    @PostMapping("/rental-requests")
    @PreAuthorize("hasAuthority('rental.request.create')")
    ResponseEntity<ApiResponse<RentalRequestResponse>> request(@Valid @RequestBody RentalRequestCreate r) {
        return ResponseEntity.status(201).body(ApiResponse.success(s.createRequest(r)));
    }

    @GetMapping("/rental-requests")
    @PreAuthorize("hasAuthority('rental.request.read')")
    ApiResponse<List<RentalRequestResponse>> requests(@RequestParam Long organizationId, @RequestParam Long branchId) {
        return ApiResponse.success(s.getRequests(organizationId, branchId));
    }

    @GetMapping("/rental-requests/{id}")
    @PreAuthorize("hasAuthority('rental.request.read')")
    ApiResponse<RentalRequestResponse> request(@PathVariable Long id) {
        return ApiResponse.success(s.getRequest(id));
    }

    @PutMapping("/rental-requests/{id}")
    @PreAuthorize("hasAuthority('rental.request.update')")
    ApiResponse<RentalRequestResponse> updateRequest(
            @PathVariable Long id, @Valid @RequestBody RentalRequestUpdate request) {
        return ApiResponse.success(s.updateRequest(id, request));
    }

    @PatchMapping("/rental-requests/{id}/cancel")
    @PreAuthorize("hasAuthority('rental.request.cancel')")
    ApiResponse<RentalRequestResponse> cancelRequest(@PathVariable Long id) {
        return ApiResponse.success(s.cancelRequest(id));
    }

    @PostMapping("/quotations")
    @PreAuthorize("hasAuthority('rental.quotation.create')")
    ResponseEntity<ApiResponse<QuotationResponse>> quotation(@Valid @RequestBody QuotationCreate r) {
        return ResponseEntity.status(201).body(ApiResponse.success(s.createQuotation(r)));
    }

    @GetMapping("/quotations")
    @PreAuthorize("hasAuthority('rental.quotation.read')")
    ApiResponse<List<QuotationResponse>> quotations(@RequestParam Long organizationId, @RequestParam Long branchId) {
        return ApiResponse.success(s.getQuotations(organizationId, branchId));
    }

    @PatchMapping("/quotations/{id}/send")
    @PreAuthorize("hasAuthority('rental.quotation.send')")
    ApiResponse<QuotationResponse> send(@PathVariable Long id) {
        return ApiResponse.success(s.sendQuotation(id));
    }

    @PatchMapping("/quotations/{id}/approve")
    @PreAuthorize("hasAuthority('rental.quotation.approve')")
    ApiResponse<QuotationResponse> approve(@PathVariable Long id) {
        return ApiResponse.success(s.approveQuotation(id));
    }

    @PatchMapping("/quotations/{id}/accept")
    @PreAuthorize("hasAuthority('rental.quotation.accept')")
    ApiResponse<QuotationResponse> accept(@PathVariable Long id) {
        return ApiResponse.success(s.acceptQuotation(id));
    }

    @PutMapping("/quotations/{id}")
    @PreAuthorize("hasAuthority('rental.quotation.update')")
    ApiResponse<QuotationResponse> updateQuotation(@PathVariable Long id, @Valid @RequestBody QuotationUpdate request) {
        return ApiResponse.success(s.updateQuotation(id, request));
    }

    @PatchMapping("/quotations/{id}/reject")
    @PreAuthorize("hasAuthority('rental.quotation.reject')")
    ApiResponse<QuotationResponse> reject(@PathVariable Long id) {
        return ApiResponse.success(s.rejectQuotation(id));
    }

    @PostMapping("/quotations/{id}/convert-to-order")
    @PreAuthorize("hasAuthority('rental.order.create')")
    ApiResponse<RentalOrderResponse> convert(@PathVariable Long id) {
        return ApiResponse.success(s.convertToOrder(id));
    }

    @GetMapping("/rental-orders")
    @PreAuthorize("hasAuthority('rental.order.read')")
    ApiResponse<List<RentalOrderResponse>> orders(@RequestParam Long organizationId, @RequestParam Long branchId) {
        return ApiResponse.success(s.getOrders(organizationId, branchId));
    }

    @PatchMapping("/rental-orders/{id}/reserve")
    @PreAuthorize("hasAuthority('inventory.reservation.create')")
    ApiResponse<RentalOrderResponse> reserve(@PathVariable Long id, @Valid @RequestBody ReserveOrderRequest r) {
        return ApiResponse.success(s.reserve(id, r));
    }

    @PatchMapping("/rental-orders/{id}/confirm")
    @PreAuthorize("hasAuthority('inventory.reservation.confirm')")
    ApiResponse<RentalOrderResponse> confirm(@PathVariable Long id) {
        return ApiResponse.success(s.confirmOrder(id));
    }

    @PatchMapping("/rental-orders/{id}/cancel")
    @PreAuthorize("hasAuthority('rental.order.cancel')")
    ApiResponse<RentalOrderResponse> cancelOrder(@PathVariable Long id, @Valid @RequestBody CancelOrderRequest r) {
        return ApiResponse.success(s.cancelOrder(id, r));
    }
}
