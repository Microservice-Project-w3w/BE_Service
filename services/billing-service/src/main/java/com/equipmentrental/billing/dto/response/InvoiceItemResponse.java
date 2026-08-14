package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemResponse {

    private Long id;
    private String itemType;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String referenceType;
    private Long referenceId;
}