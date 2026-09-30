package com.equipmentrental.inventory.dto.request;

public record ReleaseInternalReservationRequest(

        String reason,

        Long actorUserId

) {
}