package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositReportResponse {

    private Long organizationId;
    private Long branchId;

    private BigDecimal totalCollected;
    private BigDecimal totalHeld;
    private BigDecimal totalDeducted;
    private BigDecimal totalRefunded;

    private Long depositCount;
}