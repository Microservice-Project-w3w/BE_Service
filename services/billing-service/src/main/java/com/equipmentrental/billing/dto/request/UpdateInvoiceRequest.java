package com.equipmentrental.billing.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateInvoiceRequest {

    private String invoiceType;

    private LocalDateTime dueAt;
}