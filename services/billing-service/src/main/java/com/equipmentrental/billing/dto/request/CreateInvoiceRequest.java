package com.equipmentrental.billing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateInvoiceRequest {

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

    @NotBlank
    private String invoiceType;

    @NotNull
    private LocalDateTime dueAt;

    @Valid
    @NotEmpty
    private List<CreateInvoiceItemRequest> items;
}