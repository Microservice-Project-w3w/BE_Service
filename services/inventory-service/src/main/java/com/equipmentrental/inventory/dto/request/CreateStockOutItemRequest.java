package com.equipmentrental.inventory.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateStockOutItemRequest(

        @NotNull
        Long equipmentId,

        String note

) {
}