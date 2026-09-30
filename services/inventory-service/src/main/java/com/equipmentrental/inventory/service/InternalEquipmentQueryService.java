package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.response.InternalEquipmentResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.EquipmentModel;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentModelRepository;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalEquipmentQueryService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentModelRepository equipmentModelRepository;

    @Transactional(readOnly = true)
    public InternalEquipmentResponse findById(
            Long equipmentId
    ) {

        Equipment equipment =
                equipmentRepository
                        .findById(equipmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Equipment not found: "
                                                + equipmentId
                                )
                        );

        EquipmentModel model =
                equipmentModelRepository
                        .findById(
                                equipment.getModelId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Equipment model not found: "
                                                + equipment.getModelId()
                                )
                        );

        return new InternalEquipmentResponse(

                equipment.getId(),

                equipment.getOrganizationId(),

                equipment.getBranchId(),

                equipment.getWarehouseId(),

                equipment.getWarehouseLocationId(),

                model.getEquipmentTypeId(),

                equipment.getModelId(),

                equipment.getStatus(),

                equipment.getConditionStatus(),

                equipment.getAssetCode(),

                equipment.getSerialNumber(),

                equipment.getImei(),

                equipment.getMacAddress(),

                equipment.getQrCode()
        );
    }
}