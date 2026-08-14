package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DepositDeductionResponse {

    private Long id;

    private BigDecimal amount;

    private String reference;

    private LocalDateTime createdAt;
}