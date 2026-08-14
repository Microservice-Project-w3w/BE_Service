package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DebtResponse {

    private Long id;

    private Long organizationId;
    private Long branchId;
    private Long customerId;
    private Long invoiceId;

    private BigDecimal amount;
    private BigDecimal remainingAmount;

    private LocalDateTime dueAt;

    private String reason;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}