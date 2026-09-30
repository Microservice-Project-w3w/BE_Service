package com.equipmentrental.inventory.dto.request;

public record UpdateWarehouseRequest(
        String code,
        String name,
        String address
) {
}