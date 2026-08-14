package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentReportResponse {

    private Long organizationId;
    private Long branchId;

    private LocalDate fromDate;
    private LocalDate toDate;

    private BigDecimal totalAmount;
    private BigDecimal confirmedAmount;
    private BigDecimal pendingAmount;
    private BigDecimal cancelledAmount;
    private BigDecimal refundedAmount;

    private Long totalPaymentCount;
    private Long confirmedPaymentCount;
    private Long pendingPaymentCount;
    private Long cancelledPaymentCount;
}