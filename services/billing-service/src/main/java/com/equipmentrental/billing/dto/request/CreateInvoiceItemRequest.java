package com.equipmentrental.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateInvoiceItemRequest {

    @NotBlank
    private String itemType;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    private BigDecimal quantity;

    @NotNull
    @Positive
    private BigDecimal unitPrice;

    private String referenceType;

    private Long referenceId;
}