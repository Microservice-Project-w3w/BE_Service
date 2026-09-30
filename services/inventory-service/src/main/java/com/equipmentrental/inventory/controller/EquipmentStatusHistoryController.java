package com.equipmentrental.inventory.controller;


import com.equipmentrental.inventory.dto.request.CreateStatusHistoryRequest;
import com.equipmentrental.inventory.dto.response.EquipmentStatusHistoryResponse;
import com.equipmentrental.inventory.service.EquipmentStatusHistoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping(
        "/api/v1/inventory/equipment/{equipmentId}/status-history"
)
@RequiredArgsConstructor
public class EquipmentStatusHistoryController {


    private final EquipmentStatusHistoryService service;



    @PostMapping
    public EquipmentStatusHistoryResponse create(

            @PathVariable Long equipmentId,

            @RequestBody CreateStatusHistoryRequest request

    ){

        return service.create(
                equipmentId,
                request
        );

    }



    @GetMapping
    public List<EquipmentStatusHistoryResponse> findAll(

            @PathVariable Long equipmentId

    ){

        return service.findAll(
                equipmentId
        );

    }


}