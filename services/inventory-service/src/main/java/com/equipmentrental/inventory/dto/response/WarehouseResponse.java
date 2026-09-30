package com.equipmentrental.inventory.dto.response;

import java.time.LocalDateTime;

public record WarehouseResponse(
        Long id,
        Long organizationId,
        Long branchId,
        String code,
        String name,
        String address,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}