package com.equipmentrental.inventory.dto.request;

import com.equipmentrental.inventory.enums.EquipmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEquipmentStatusRequest(

        @NotNull(message = "status không được để trống")
        EquipmentStatus status
) {
}