package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InternalRentalOrderBillingStatusResponse {

    private Long rentalOrderId;

    private BigDecimal invoiceTotal;

    private BigDecimal paidAmount;

    private BigDecimal outstandingAmount;

    private BigDecimal depositHeld;

    private boolean fullyPaid;
}