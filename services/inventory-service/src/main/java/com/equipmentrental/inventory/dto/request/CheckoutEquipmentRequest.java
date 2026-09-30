package com.equipmentrental.inventory.dto.request;

public record CheckoutEquipmentRequest(

        Long rentalOrderId,

        Long organizationId,

        Long branchId,

        String checklistReference,

        Long actorUserId

) {
}