package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoicePaymentStatusResponse {

    private Long invoiceId;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private BigDecimal remainingAmount;

    private String paymentStatus;
}