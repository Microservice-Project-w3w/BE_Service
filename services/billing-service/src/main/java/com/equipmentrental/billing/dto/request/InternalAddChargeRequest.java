package com.equipmentrental.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InternalAddChargeRequest {

    @NotBlank
    private String requestReference;

    @NotBlank
    private String chargeType;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String referenceType;

    private Long referenceId;

    @NotBlank
    private String description;
}