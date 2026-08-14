package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {

    private Long id;
    private Long organizationId;
    private Long branchId;
    private Long customerId;
    private Long invoiceId;

    private BigDecimal amount;

    private String paymentMethod;
    private String transactionReference;
    private String status;

    private LocalDateTime paidAt;
}