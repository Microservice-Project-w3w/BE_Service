package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DepositHistoryResponse {

    private Long id;

    private Long depositId;

    private String action;

    private BigDecimal amount;

    private String oldStatus;

    private String newStatus;

    private String description;

    private Long actorUserId;

    private LocalDateTime createdAt;
}