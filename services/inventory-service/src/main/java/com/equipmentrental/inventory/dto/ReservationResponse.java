package com.equipmentrental.inventory.dto;

import com.equipmentrental.inventory.enums.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long reservationId,
        Long equipmentId,
        Long rentalId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime expiresAt,
        ReservationStatus status
) {
}