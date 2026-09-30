package com.equipmentrental.inventory.dto.response;

import java.time.LocalDateTime;

public record EquipmentTypeResponse(

        Long id,

        Long organizationId,

        Long categoryId,

        String code,

        String name,

        String description,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}