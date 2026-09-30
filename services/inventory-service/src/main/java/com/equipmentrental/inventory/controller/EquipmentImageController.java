package com.equipmentrental.inventory.controller;


import com.equipmentrental.inventory.dto.request.CreateEquipmentImageRequest;
import com.equipmentrental.inventory.service.EquipmentImageService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/inventory/equipment-images")
@RequiredArgsConstructor
public class EquipmentImageController {


    private final EquipmentImageService service;



    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreateEquipmentImageRequest request
    ){

        return ResponseEntity.ok(
                service.create(request)
        );
    }



    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<?> getByEquipment(
            @PathVariable Long equipmentId
    ){

        return ResponseEntity.ok(
                service.getByEquipment(equipmentId)
        );
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id
    ){

        service.delete(id);

        return ResponseEntity.noContent()
                .build();
    }

}