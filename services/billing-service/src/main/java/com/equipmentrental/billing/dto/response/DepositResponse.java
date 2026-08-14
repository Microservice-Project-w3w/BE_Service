package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DepositResponse {

    private Long id;

    private Long organizationId;
    private Long branchId;
    private Long customerId;
    private Long rentalOrderId;
    private Long rentalContractId;

    private BigDecimal amount;
    private BigDecimal deductedAmount;
    private BigDecimal refundedAmount;
    private BigDecimal remainingAmount;

    private String paymentMethod;
    private String reference;
    private String notes;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}