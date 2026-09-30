package com.equipmentrental.inventory.dto.request;

public record CreateWarehouseRequest(
        Long organizationId,
        Long branchId,
        String code,
        String name,
        String address,
        Boolean active
) {
}