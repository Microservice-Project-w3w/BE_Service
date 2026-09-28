package com.equipmentrental.inventory.dto.response;

import java.time.LocalDateTime;

public record BrandResponse(

        Long id,
        Long organizationId,
        String code,
        String name,
        String description,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}