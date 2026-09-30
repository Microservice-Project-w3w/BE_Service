package com.equipmentrental.inventory.dto.request;

public record UpdateEquipmentAccessoryRequest(

        String name,

        String serialNumber,

        Integer quantity,

        Boolean requiredOnReturn,

        String note

) {
}