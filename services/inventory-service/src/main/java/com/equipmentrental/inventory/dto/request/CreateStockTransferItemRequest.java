package com.equipmentrental.inventory.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateStockTransferItemRequest(

        @NotNull
        Long equipmentId,

        String note

) {
}