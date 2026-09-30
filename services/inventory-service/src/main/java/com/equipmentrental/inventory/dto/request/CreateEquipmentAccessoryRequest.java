package com.equipmentrental.inventory.dto.request;


public record CreateEquipmentAccessoryRequest(

        String name,

        String serialNumber,

        Integer quantity,

        Boolean requiredOnReturn,

        String note

) {}