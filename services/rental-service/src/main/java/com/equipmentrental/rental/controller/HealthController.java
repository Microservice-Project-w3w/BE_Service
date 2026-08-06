package com.equipmentrental.rental.controller;

import com.equipmentrental.common.web.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of("service", "rental-service", "status", "UP", "port", 8084));
    }
}
