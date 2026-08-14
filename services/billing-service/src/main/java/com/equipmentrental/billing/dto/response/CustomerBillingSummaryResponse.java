package com.equipmentrental.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBillingSummaryResponse {

    private Long customerId;

    private BigDecimal totalInvoiced;

    private BigDecimal totalPaid;

    private BigDecimal outstandingDebt;

    private BigDecimal depositHeld;
}