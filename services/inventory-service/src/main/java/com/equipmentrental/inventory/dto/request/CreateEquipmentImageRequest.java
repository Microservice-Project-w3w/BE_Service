package com.equipmentrental.inventory.dto.request;


public record CreateEquipmentImageRequest(

        Long equipmentId,

        String imageUrl,

        Boolean primaryImage,

        Integer displayOrder

) {}