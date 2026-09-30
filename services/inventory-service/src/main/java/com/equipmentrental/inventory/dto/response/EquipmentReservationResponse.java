package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.ReservationStatus;

import java.time.LocalDateTime;

public record EquipmentReservationResponse(

        Long id,

        Long organizationId,

        Long branchId,

        String reservationCode,

        String requestReference,

        Long rentalOrderId,

        LocalDateTime startAt,

        LocalDateTime endAt,

        LocalDateTime expiresAt,

        ReservationStatus status,

        Long createdBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        Long version

) {
}