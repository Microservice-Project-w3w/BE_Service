package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InternalContractSettlementStatusResponse {

    private Long contractId;

    private BigDecimal outstandingAmount;

    private BigDecimal depositRemaining;

    private boolean hasUnresolvedPayment;

    private boolean canLiquidate;
}