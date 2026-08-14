package com.equipmentrental.billing.client;

import com.equipmentrental.billing.dto.external.LogisticsReturnRecordResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "logistics-service", url = "${app.services.logistics}")
public interface LogisticsClient {

    @GetMapping("/api/v1/returns/rental-order/{rentalOrderId}")
    LogisticsReturnRecordResponse getReturnRecordByRentalOrderId(@PathVariable("rentalOrderId") Long rentalOrderId);
}
