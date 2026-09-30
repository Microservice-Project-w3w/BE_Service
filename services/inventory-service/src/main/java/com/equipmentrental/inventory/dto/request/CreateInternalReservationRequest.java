package com.equipmentrental.inventory.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record CreateInternalReservationRequest(

        String requestReference,

        Long organizationId,

        Long branchId,

        Long rentalOrderId,

        LocalDateTime startAt,

        LocalDateTime endAt,

        List<CreateInternalReservationItemRequest> items

) {
}