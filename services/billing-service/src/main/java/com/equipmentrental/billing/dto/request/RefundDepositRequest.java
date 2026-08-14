package com.equipmentrental.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundDepositRequest {

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String paymentMethod;

    @NotBlank
    private String reason;

    private Long actorUserId;
}