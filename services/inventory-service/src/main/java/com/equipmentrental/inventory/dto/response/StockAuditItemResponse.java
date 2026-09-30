package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.StockAuditResult;

import java.time.LocalDateTime;

public record StockAuditItemResponse(

        Long id,

        Long equipmentId,

        Long expectedWarehouseId,

        Long expectedLocationId,

        Long actualWarehouseId,

        Long actualLocationId,

        StockAuditResult result,

        String note,

        Long checkedBy,

        LocalDateTime checkedAt

) {
}