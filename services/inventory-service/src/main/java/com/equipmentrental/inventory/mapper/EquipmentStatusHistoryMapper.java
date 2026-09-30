package com.equipmentrental.inventory.mapper;


import com.equipmentrental.inventory.dto.response.EquipmentStatusHistoryResponse;
import com.equipmentrental.inventory.entity.EquipmentStatusHistory;


public class EquipmentStatusHistoryMapper {


    public static EquipmentStatusHistoryResponse toResponse(
            EquipmentStatusHistory entity
    ){

        return new EquipmentStatusHistoryResponse(

                entity.getId(),

                entity.getEquipmentId(),

                entity.getOldStatus(),

                entity.getNewStatus(),

                entity.getReason(),

                entity.getChangedBy(),

                entity.getChangedAt()

        );

    }


}