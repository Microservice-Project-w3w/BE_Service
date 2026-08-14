package com.equipmentrental.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelInvoiceRequest {

    @NotBlank
    private String reason;
}