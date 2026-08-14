package com.equipmentrental.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDepositRequest {

    @NotNull
    private Long organizationId;

    @NotNull
    private Long branchId;

    @NotNull
    private Long customerId;

    @NotNull
    private Long rentalOrderId;

    @NotNull
    private Long rentalContractId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String paymentMethod;

    private String reference;

    private String notes;
}