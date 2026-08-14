package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRefundResponse {

    private Long id;
    private Long paymentId;
    private BigDecimal amount;
    private String reason;
    private Long actorUserId;
    private LocalDateTime refundedAt;
}