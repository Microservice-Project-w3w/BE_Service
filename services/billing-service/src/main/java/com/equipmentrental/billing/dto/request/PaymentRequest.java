package com.equipmentrental.billing.dto.request;

import com.equipmentrental.billing.entity.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotBlank
    private String paymentReference;
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    
    @NotNull
    private PaymentMethod paymentMethod;
}
