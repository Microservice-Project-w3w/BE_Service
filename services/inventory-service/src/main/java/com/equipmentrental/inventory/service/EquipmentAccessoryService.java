package com.equipmentrental.inventory.service;


import com.equipmentrental.inventory.dto.request.*;
import com.equipmentrental.inventory.dto.response.EquipmentAccessoryResponse;
import com.equipmentrental.inventory.entity.EquipmentAccessory;
import com.equipmentrental.inventory.repository.EquipmentAccessoryRepository;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class EquipmentAccessoryService {


    private final EquipmentAccessoryRepository repository;



    public EquipmentAccessoryResponse create(
            Long equipmentId,
            CreateEquipmentAccessoryRequest request
    ){


        EquipmentAccessory accessory =
                EquipmentAccessory.builder()
                        .equipmentId(equipmentId)
                        .name(request.name())
                        .serialNumber(request.serialNumber())
                        .quantity(request.quantity())
                        .requiredOnReturn(request.requiredOnReturn())
                        .note(request.note())
                        .build();


        return map(
                repository.save(accessory)
        );

    }




    public List<EquipmentAccessoryResponse> findAll(
            Long equipmentId
    ){

        return repository
                .findByEquipmentId(equipmentId)
                .stream()
                .map(this::map)
                .toList();

    }





    public EquipmentAccessoryResponse findById(
            Long equipmentId,
            Long accessoryId
    ) {

        EquipmentAccessory accessory =
                repository.findById(accessoryId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Accessory not found"
                                )
                        );


        if (!accessory.getEquipmentId().equals(equipmentId)) {

            throw new ResourceNotFoundException(
                    "Accessory does not belong to equipment"
            );
        }


        return map(accessory);
    }





    public EquipmentAccessoryResponse update(
            Long equipmentId,
            Long accessoryId,
            UpdateEquipmentAccessoryRequest request
    ) {

        EquipmentAccessory accessory =
                repository.findByIdAndEquipmentId(accessoryId, equipmentId)
                        .orElseThrow(() ->
                                new RuntimeException("Accessory not found")
                        );


        accessory.setName(request.name());

        accessory.setSerialNumber(
                request.serialNumber()
        );

        accessory.setQuantity(
                request.quantity()
        );

        accessory.setRequiredOnReturn(
                request.requiredOnReturn()
        );


        accessory.setNote(
                request.note()
        );


        repository.save(accessory);


        return toResponse(accessory);
    }





    public void delete(
            Long equipmentId,
            Long accessoryId
    ){

        EquipmentAccessory accessory =
                repository
                        .findByIdAndEquipmentId(
                                accessoryId,
                                equipmentId
                        )
                        .orElseThrow();


        repository.delete(accessory);

    }





    private EquipmentAccessoryResponse map(
            EquipmentAccessory accessory
    ) {

        return new EquipmentAccessoryResponse(

                accessory.getId(),

                accessory.getEquipmentId(),

                accessory.getName(),

                accessory.getSerialNumber(),

                accessory.getQuantity(),

                accessory.getRequiredOnReturn(),

                accessory.getNote(),

                accessory.getCreatedAt()

        );
    }
    private EquipmentAccessoryResponse toResponse(
            EquipmentAccessory accessory
    ) {

        return new EquipmentAccessoryResponse(
                accessory.getId(),
                accessory.getEquipmentId(),
                accessory.getName(),
                accessory.getSerialNumber(),
                accessory.getQuantity(),
                accessory.getRequiredOnReturn(),
                accessory.getNote(),
                accessory.getCreatedAt()
        );

    }
}