package com.equipmentrental.billing.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvoiceHistoryResponse {

    private Long id;

    private Long invoiceId;

    private String action;

    private String oldStatus;

    private String newStatus;

    private String description;

    private Long actorUserId;

    private LocalDateTime createdAt;
}