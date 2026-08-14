package com.equipmentrental.billing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InternalCreateInvoiceRequest {

    @NotBlank
    private String requestReference;

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
    private LocalDateTime dueAt;

    @Valid
    @NotEmpty
    private List<InternalInvoiceItemRequest> items;
}