package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.StockOutStatus;

import java.time.LocalDateTime;
import java.util.List;

public record StockOutResponse(

        Long id,

        Long organizationId,

        Long branchId,

        Long warehouseId,

        String stockOutCode,

        String purposeType,

        String referenceCode,

        String note,

        StockOutStatus status,

        Long createdBy,

        Long confirmedBy,

        LocalDateTime createdAt,

        LocalDateTime confirmedAt,

        LocalDateTime cancelledAt,

        List<StockOutItemResponse> items

) {
}