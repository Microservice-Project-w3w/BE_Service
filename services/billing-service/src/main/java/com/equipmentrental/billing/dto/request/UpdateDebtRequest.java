package com.equipmentrental.billing.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateDebtRequest {

    private BigDecimal amount;

    private LocalDateTime dueAt;

    private String reason;

    private String status;
}