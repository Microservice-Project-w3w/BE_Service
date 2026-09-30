package com.equipmentrental.inventory.controller;


import com.equipmentrental.inventory.dto.request.*;
import com.equipmentrental.inventory.service.EquipmentAccessoryService;


import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(
        "/api/v1/inventory/equipment/{equipmentId}/accessories"
)
@RequiredArgsConstructor
public class EquipmentAccessoryController {


    private final EquipmentAccessoryService service;



    @PostMapping
    public ResponseEntity<?> create(

            @PathVariable Long equipmentId,

            @RequestBody CreateEquipmentAccessoryRequest request

    ){

        return ResponseEntity.ok(
                service.create(
                        equipmentId,
                        request
                )
        );

    }



    @GetMapping
    public ResponseEntity<?> findAll(

            @PathVariable Long equipmentId

    ){

        return ResponseEntity.ok(
                service.findAll(
                        equipmentId
                )
        );

    }




    @GetMapping("/{accessoryId}")
    public ResponseEntity<?> findById(

            @PathVariable Long equipmentId,

            @PathVariable Long accessoryId

    ){

        return ResponseEntity.ok(
                service.findById(
                        equipmentId,
                        accessoryId
                )
        );

    }





    @PutMapping("/{accessoryId}")
    public ResponseEntity<?> update(

            @PathVariable Long equipmentId,

            @PathVariable Long accessoryId,

            @RequestBody UpdateEquipmentAccessoryRequest request

    ){

        return ResponseEntity.ok(
                service.update(
                        equipmentId,
                        accessoryId,
                        request
                )
        );

    }





    @DeleteMapping("/{accessoryId}")
    public ResponseEntity<?> delete(

            @PathVariable Long equipmentId,

            @PathVariable Long accessoryId

    ){

        service.delete(
                equipmentId,
                accessoryId
        );


        return ResponseEntity.noContent()
                .build();

    }

}