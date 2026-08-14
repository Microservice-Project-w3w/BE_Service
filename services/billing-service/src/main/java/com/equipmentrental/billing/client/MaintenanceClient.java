package com.equipmentrental.billing.client;

import com.equipmentrental.billing.dto.external.MaintenanceEvaluationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "maintenance-service", url = "${app.services.maintenance}")
public interface MaintenanceClient {

    @GetMapping("/api/v1/evaluations/rental-order/{rentalOrderId}")
    MaintenanceEvaluationResponse getEvaluationByRentalOrderId(@PathVariable("rentalOrderId") Long rentalOrderId);
}
