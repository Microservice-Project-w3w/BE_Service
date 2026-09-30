package com.equipmentrental.inventory.service;


import com.equipmentrental.inventory.dto.request.CreateStatusHistoryRequest;
import com.equipmentrental.inventory.dto.response.EquipmentStatusHistoryResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.EquipmentStatusHistory;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.mapper.EquipmentStatusHistoryMapper;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.repository.EquipmentStatusHistoryRepository;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class EquipmentStatusHistoryService {


    private final EquipmentStatusHistoryRepository historyRepository;


    private final EquipmentRepository equipmentRepository;



    // tạo lịch sử đổi trạng thái

    public EquipmentStatusHistoryResponse create(
            Long equipmentId,
            CreateStatusHistoryRequest request
    ){


        Equipment equipment =
                equipmentRepository.findById(equipmentId)
                        .orElseThrow(
                                ()-> new ResourceNotFoundException(
                                        "Equipment not found"
                                )
                        );



        EquipmentStatus oldStatus =
                equipment.getStatus();



        equipment.setStatus(
                request.newStatus()
        );


        equipmentRepository.save(equipment);



        EquipmentStatusHistory history =
                EquipmentStatusHistory.builder()

                        .equipmentId(equipmentId)

                        .oldStatus(oldStatus)

                        .newStatus(request.newStatus())

                        .reason(request.reason())

                        .changedBy(request.changedBy())

                        .build();



        return EquipmentStatusHistoryMapper.toResponse(

                historyRepository.save(history)

        );


    }




    public List<EquipmentStatusHistoryResponse>
    findAll(Long equipmentId){


        return historyRepository
                .findByEquipmentIdOrderByChangedAtDesc(
                        equipmentId
                )

                .stream()

                .map(
                        EquipmentStatusHistoryMapper::toResponse
                )

                .toList();

    }


}