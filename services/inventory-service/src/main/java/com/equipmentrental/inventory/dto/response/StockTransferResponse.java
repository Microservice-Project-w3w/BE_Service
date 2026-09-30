package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.StockTransferStatus;

import java.time.LocalDateTime;
import java.util.List;

public record StockTransferResponse(

        Long id,

        Long organizationId,

        String transferCode,

        Long fromBranchId,

        Long sourceWarehouseId,

        Long toBranchId,

        Long destinationWarehouseId,

        StockTransferStatus status,

        String note,

        Long createdBy,

        Long approvedBy,

        Long receivedBy,

        LocalDateTime createdAt,

        LocalDateTime approvedAt,

        LocalDateTime dispatchedAt,

        LocalDateTime receivedAt,

        LocalDateTime cancelledAt,

        List<StockTransferItemResponse> items

) {
}