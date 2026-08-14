package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceResponse {

    private Long id;

    private Long organizationId;
    private Long branchId;
    private Long customerId;
    private Long rentalOrderId;
    private Long rentalContractId;

    private String invoiceType;
    private String status;

    private BigDecimal subtotal;
    private BigDecimal totalAmount;

    private LocalDateTime dueAt;

    private List<InvoiceItemResponse> items;

    
}