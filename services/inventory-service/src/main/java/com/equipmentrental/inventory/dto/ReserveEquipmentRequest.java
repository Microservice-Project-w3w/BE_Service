package com.equipmentrental.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReserveEquipmentRequest(
        @NotNull Long equipmentId,
        @NotNull Long rentalId,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt
) {
}