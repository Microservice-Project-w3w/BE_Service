package com.equipmentrental.billing.client;

import com.equipmentrental.billing.dto.external.RentalOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "rental-service", url = "${app.services.rental}")
public interface RentalClient {

    @GetMapping("/api/v1/rentals/{rentalOrderId}")
    RentalOrderResponse getRentalOrder(@PathVariable("rentalOrderId") Long rentalOrderId);
    
    @GetMapping("/api/v1/contracts/{contractId}")
    RentalOrderResponse getContract(@PathVariable("contractId") Long contractId);
}
