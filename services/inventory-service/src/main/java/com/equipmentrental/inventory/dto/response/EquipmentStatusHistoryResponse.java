package com.equipmentrental.inventory.dto.response;


import com.equipmentrental.inventory.enums.EquipmentStatus;

import java.time.LocalDateTime;


public record EquipmentStatusHistoryResponse(

        Long id,

        Long equipmentId,

        EquipmentStatus oldStatus,

        EquipmentStatus newStatus,

        String reason,

        Long changedBy,

        LocalDateTime changedAt

){}