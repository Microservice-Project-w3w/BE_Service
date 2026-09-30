package com.equipmentrental.inventory.dto.response;

import java.time.LocalDateTime;

public record StockTransferItemResponse(

        Long id,

        Long equipmentId,

        String note,

        LocalDateTime createdAt

) {
}