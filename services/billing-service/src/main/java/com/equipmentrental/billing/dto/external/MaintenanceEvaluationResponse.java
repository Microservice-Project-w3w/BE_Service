package com.equipmentrental.billing.dto.external;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaintenanceEvaluationResponse {
    private Long id;
    private Long rentalOrderId;
    private BigDecimal totalDamageCost;
    private String evaluationDetails;
}
