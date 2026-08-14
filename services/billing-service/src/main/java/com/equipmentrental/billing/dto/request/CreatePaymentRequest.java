package com.equipmentrental.billing.dto.request;

import com.equipmentrental.billing.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreatePaymentRequest {

    @NotNull
    private Long organizationId;

    @NotNull
    private Long branchId;

    @NotNull
    private Long customerId;

    @NotNull
    private Long invoiceId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private PaymentMethod paymentMethod;

    @NotNull
    private String transactionReference;

    @NotNull
    private LocalDateTime paidAt;
}