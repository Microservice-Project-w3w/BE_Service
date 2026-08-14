package com.equipmentrental.billing.client;

import com.equipmentrental.billing.dto.external.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "${app.services.customer}")
public interface CustomerClient {

    @GetMapping("/api/v1/customers/{customerId}")
    CustomerResponse getCustomerById(@PathVariable("customerId") Long customerId);
}
