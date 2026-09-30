package com.equipmentrental.inventory.dto.request;

import com.equipmentrental.inventory.enums.StockAuditResult;
import jakarta.validation.constraints.NotNull;

public record RecordStockAuditItemRequest(

        @NotNull
        Long equipmentId,

        @NotNull
        StockAuditResult result,

        Long actualWarehouseId,

        Long actualLocationId,

        String note,

        Long checkedBy

) {
}