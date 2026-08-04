package com.equipmentrental.maintenance.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("service", "maintenance-service", "status", "UP", "port", 8087);
    }
}
