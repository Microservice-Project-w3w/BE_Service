package com.equipmentrental.inventory.dto;

import com.equipmentrental.inventory.enums.EquipmentStatus;

public record AvailabilityResponse(
        Long equipmentId,
        boolean available,
        EquipmentStatus equipmentStatus,
        String reason
) {
}