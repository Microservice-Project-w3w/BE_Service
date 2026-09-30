package com.equipmentrental.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStockAuditRequest(

        @NotNull
        Long organizationId,

        @NotNull
        Long branchId,

        @NotNull
        Long warehouseId,

        @NotBlank
        String auditCode,

        String note,

        Long createdBy

) {
}