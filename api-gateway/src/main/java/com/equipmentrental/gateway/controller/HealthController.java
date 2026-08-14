package com.equipmentrental.gateway.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final String serviceName;
    private final int serverPort;

    public HealthController(
            @Value("${spring.application.name:api-gateway}") String serviceName,
            @Value("${server.port:8080}") int serverPort) {
        this.serviceName = serviceName;
        this.serverPort = serverPort;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", serviceName);
        response.put("status", "UP");
        response.put("port", serverPort);
        return response;
    }
}
