package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.StockAuditStatus;

import java.time.LocalDateTime;
import java.util.List;

public record StockAuditResponse(

        Long id,

        Long organizationId,

        Long branchId,

        Long warehouseId,

        String auditCode,

        StockAuditStatus status,

        String note,

        Long createdBy,

        Long startedBy,

        Long completedBy,

        LocalDateTime createdAt,

        LocalDateTime startedAt,

        LocalDateTime completedAt,

        LocalDateTime cancelledAt,

        long totalItems,

        long checkedItems,

        long foundCount,

        long missingCount,

        long damagedCount,

        long wrongLocationCount,

        List<StockAuditItemResponse> items

) {
}