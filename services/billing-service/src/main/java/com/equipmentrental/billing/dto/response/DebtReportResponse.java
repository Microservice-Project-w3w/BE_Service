package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DebtReportResponse {

    private Long organizationId;
    private Long branchId;

    private BigDecimal totalDebt;
    private BigDecimal overdueDebt;

    private Long customerCount;
    private Long debtCount;
    private Long overdueDebtCount;
}