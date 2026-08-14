package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InternalCustomerDebtStatusResponse {

    private Long customerId;

    private boolean hasDebt;

    private BigDecimal totalOutstanding;

    private BigDecimal overdueAmount;
}