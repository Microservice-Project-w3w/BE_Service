package com.equipmentrental.logistics.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DeliveryFeeRuleResponse {
    private Long id;
    private String name;
    private BigDecimal baseFee;
    private BigDecimal maxDistanceKm;
    private BigDecimal extraFeePerKm;
    private Boolean isActive;
    private Long organizationId;
    private Long branchId;
}
