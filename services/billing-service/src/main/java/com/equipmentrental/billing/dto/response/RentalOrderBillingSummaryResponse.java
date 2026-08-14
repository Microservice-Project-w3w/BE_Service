package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RentalOrderBillingSummaryResponse {

    private Long rentalOrderId;

    private BigDecimal totalInvoiceAmount;

    private BigDecimal paidAmount;

    private BigDecimal remainingAmount;

    private BigDecimal depositAmount;

    private BigDecimal deductedDeposit;

    private BigDecimal refundedDeposit;

    private BigDecimal outstandingDebt;
}