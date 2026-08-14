package com.equipmentrental.billing.dto.request;

import com.equipmentrental.billing.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SettleDebtRequest {

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private PaymentMethod paymentMethod;

    @NotNull
    private Long actorUserId;
}