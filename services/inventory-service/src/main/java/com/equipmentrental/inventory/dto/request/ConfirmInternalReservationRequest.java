package com.equipmentrental.inventory.dto.request;

public record ConfirmInternalReservationRequest(
        Long actorUserId,
        Long rentalOrderId
) {
}
