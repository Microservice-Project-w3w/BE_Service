package com.equipmentrental.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AvailabilityRequest(
        @NotNull Long equipmentId,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt
) {
}