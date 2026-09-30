package com.equipmentrental.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateStockInRequest(

        @NotNull
        Long organizationId,

        @NotNull
        Long branchId,

        @NotNull
        Long warehouseId,

        @NotBlank
        String stockInCode,

        String sourceType,

        String referenceCode,

        String note,

        Long createdBy,

        @NotEmpty
        List<@Valid CreateStockInItemRequest> items

) {
}