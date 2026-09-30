package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.StockInStatus;

import java.time.LocalDateTime;
import java.util.List;

public record StockInResponse(

        Long id,

        Long organizationId,

        Long branchId,

        Long warehouseId,

        String stockInCode,

        String sourceType,

        String referenceCode,

        String note,

        StockInStatus status,

        Long createdBy,

        Long confirmedBy,

        LocalDateTime createdAt,

        LocalDateTime confirmedAt,

        LocalDateTime cancelledAt,

        List<StockInItemResponse> items

) {
}