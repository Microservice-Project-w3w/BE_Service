package com.equipmentrental.inventory.dto.response;

import com.equipmentrental.inventory.enums.EquipmentStatus;

public record CheckoutEquipmentResponse(

        Long equipmentId,

        Long rentalOrderId,

        Long organizationId,

        Long branchId,

        String checklistReference,

        EquipmentStatus oldStatus,

        EquipmentStatus newStatus,

        Long actorUserId

) {
}