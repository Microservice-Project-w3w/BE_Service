package com.equipmentrental.inventory.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateStockInItemRequest(

        @NotNull
        Long equipmentId,

        String note

) {
}