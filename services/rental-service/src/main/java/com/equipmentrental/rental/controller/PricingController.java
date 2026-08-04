package com.equipmentrental.rental.controller;

import com.equipmentrental.rental.dto.*;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.http.*;
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
    ResponseEntity<RentalPrice> createPrice(@Valid @RequestBody RentalPriceRequest r) {
        return ResponseEntity.status(201).body(s.createPrice(r));
    }

    @GetMapping("/rental-prices")
    List<RentalPrice> prices() {
        return s.getPrices();
    }

    @PutMapping("/rental-prices/{id}")
    RentalPrice update(@PathVariable Long id, @Valid @RequestBody RentalPriceRequest r) {
        return s.updatePrice(id, r);
    }

    @PostMapping("/delivery-fees")
    ResponseEntity<DeliveryFeeRule> fee(@Valid @RequestBody DeliveryFeeRuleRequest r) {
        return ResponseEntity.status(201).body(s.createFee(r));
    }

    @GetMapping("/delivery-fees")
    List<DeliveryFeeRule> fees() {
        return s.getFees();
    }

    @PostMapping("/discount-codes")
    ResponseEntity<DiscountCode> discount(@Valid @RequestBody DiscountCodeRequest r) {
        return ResponseEntity.status(201).body(s.createDiscount(r));
    }

    @GetMapping("/discount-codes")
    List<DiscountCode> discounts() {
        return s.getDiscounts();
    }
}
