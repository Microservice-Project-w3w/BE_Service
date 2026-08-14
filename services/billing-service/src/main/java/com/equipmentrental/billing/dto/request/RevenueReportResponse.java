package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RevenueReportResponse {

    private Long organizationId;
    private Long branchId;

    private LocalDate fromDate;
    private LocalDate toDate;

    private BigDecimal totalInvoiced;
    private BigDecimal totalPaid;
    private BigDecimal totalRefunded;
    private BigDecimal netRevenue;

    private Long invoiceCount;
    private Long paymentCount;
}