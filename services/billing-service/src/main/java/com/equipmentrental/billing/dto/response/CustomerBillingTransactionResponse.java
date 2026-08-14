package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerBillingTransactionResponse {

    private String transactionType;

    private Long referenceId;

    private BigDecimal amount;

    private String status;

    private String description;

    private LocalDateTime occurredAt;
}