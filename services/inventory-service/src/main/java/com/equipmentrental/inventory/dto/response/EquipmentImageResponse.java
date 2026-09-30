package com.equipmentrental.inventory.dto.response;


public record EquipmentImageResponse(

        Long id,

        Long equipmentId,

        String imageUrl,

        Boolean primaryImage,

        Integer displayOrder

) {}