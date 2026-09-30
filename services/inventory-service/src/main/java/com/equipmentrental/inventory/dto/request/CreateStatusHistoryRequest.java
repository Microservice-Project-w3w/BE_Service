package com.equipmentrental.inventory.dto.request;


import jakarta.validation.constraints.NotBlank;
import com.equipmentrental.inventory.enums.EquipmentStatus;

public record CreateStatusHistoryRequest(

        EquipmentStatus newStatus,

        String reason,

        Long changedBy

) {}