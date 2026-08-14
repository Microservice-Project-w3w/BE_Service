package com.equipmentrental.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InternalInvoiceItemRequest {

    @NotBlank
    private String type;

    @NotNull
    @Positive
    private BigDecimal amount;
}