package com.equipmentrental.logistics.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateDeliveryFeeRuleRequest {

    @NotNull
    private Long organizationId;

    private Long branchId;

    @NotBlank
    private String name;

    @NotNull
    private BigDecimal baseFee;

    @NotNull
    private BigDecimal maxDistanceKm;

    @NotNull
    private BigDecimal extraFeePerKm;

    @NotNull
    private Boolean isActive;
}
