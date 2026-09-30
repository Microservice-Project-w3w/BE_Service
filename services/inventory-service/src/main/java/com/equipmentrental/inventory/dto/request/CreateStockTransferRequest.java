package com.equipmentrental.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateStockTransferRequest(

        @NotNull
        Long organizationId,

        @NotNull
        Long sourceWarehouseId,

        @NotNull
        Long destinationWarehouseId,

        @NotBlank
        String transferCode,

        String note,

        Long createdBy,

        @NotEmpty
        List<@Valid CreateStockTransferItemRequest> items

) {
}