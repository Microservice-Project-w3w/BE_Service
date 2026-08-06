package com.equipmentrental.rental.controller;

import com.equipmentrental.common.web.ApiResponse;
import com.equipmentrental.rental.dto.request.DiscountCodeRequest;
import com.equipmentrental.rental.dto.request.RentalPriceRequest;
import com.equipmentrental.rental.dto.response.DiscountCodeResponse;
import com.equipmentrental.rental.dto.response.RentalPriceResponse;
import com.equipmentrental.rental.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PricingController {
    private final PricingService s;

    public PricingController(PricingService s) {
        this.s = s;
    }

    @PostMapping("/rental-prices")
    @PreAuthorize("hasAuthority('rental.pricing.manage')")
    ResponseEntity<ApiResponse<RentalPriceResponse>> createPrice(@Valid @RequestBody RentalPriceRequest r) {
        return ResponseEntity.status(201).body(ApiResponse.success(s.createPrice(r)));
    }

    @GetMapping("/rental-prices")
    @PreAuthorize("hasAuthority('rental.pricing.read')")
    ApiResponse<List<RentalPriceResponse>> prices(@RequestParam Long organizationId, @RequestParam Long branchId) {
        return ApiResponse.success(s.getPrices(organizationId, branchId));
    }

    @PutMapping("/rental-prices/{id}")
    @PreAuthorize("hasAuthority('rental.pricing.manage')")
    ApiResponse<RentalPriceResponse> update(@PathVariable Long id, @Valid @RequestBody RentalPriceRequest r) {
        return ApiResponse.success(s.updatePrice(id, r));
    }

    @PostMapping("/discount-codes")
    @PreAuthorize("hasAuthority('rental.discount.manage')")
    ResponseEntity<ApiResponse<DiscountCodeResponse>> discount(@Valid @RequestBody DiscountCodeRequest r) {
        return ResponseEntity.status(201).body(ApiResponse.success(s.createDiscount(r)));
    }

    @GetMapping("/discount-codes")
    @PreAuthorize("hasAuthority('rental.discount.read')")
    ApiResponse<List<DiscountCodeResponse>> discounts(@RequestParam Long organizationId, @RequestParam Long branchId) {
        return ApiResponse.success(s.getDiscounts(organizationId, branchId));
    }

    @PutMapping("/discount-codes/{id}")
    @PreAuthorize("hasAuthority('rental.discount.manage')")
    ApiResponse<DiscountCodeResponse> updateDiscount(@PathVariable Long id, @Valid @RequestBody DiscountCodeRequest request) {
        return ApiResponse.success(s.updateDiscount(id, request));
    }

    @PatchMapping("/discount-codes/{id}/active")
    @PreAuthorize("hasAuthority('rental.discount.manage')")
    ApiResponse<DiscountCodeResponse> setDiscountActive(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(s.setDiscountActive(id, active));
    }

    @DeleteMapping("/discount-codes/{id}")
    @PreAuthorize("hasAuthority('rental.discount.manage')")
    ApiResponse<Void> deleteDiscount(@PathVariable Long id) {
        s.deleteDiscount(id);
        return ApiResponse.success(null);
    }
}
