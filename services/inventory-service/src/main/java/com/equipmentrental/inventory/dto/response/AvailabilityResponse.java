package com.equipmentrental.inventory.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AvailabilityResponse(
        Long organizationId,
        Long branchId,
        Long equipmentTypeId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer requestedQuantity,
        Integer totalPhysical,
        Integer unavailableByStatus,
        Integer reservedQuantity,
        Integer availableQuantity,
        Boolean available,
        List<Long> availableEquipmentIds
) {
}