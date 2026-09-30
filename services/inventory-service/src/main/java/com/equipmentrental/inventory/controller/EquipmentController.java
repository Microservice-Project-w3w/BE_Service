package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateEquipmentRequest;
import com.equipmentrental.inventory.dto.response.EquipmentResponse;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentStatusRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService service;

    @PostMapping
    public ResponseEntity<EquipmentResponse> create(
            @Valid
            @RequestBody
            CreateEquipmentRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public List<EquipmentResponse> getAll(
            @RequestParam Long organizationId,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Long warehouseId,

            @RequestParam(required = false)
            Long modelId,

            @RequestParam(required = false)
            EquipmentStatus status
    ) {

        return service.getAll(
                organizationId,
                branchId,
                warehouseId,
                modelId,
                status
        );
    }

    @GetMapping("/{id}")
    public EquipmentResponse getById(
            @PathVariable Long id,

            @RequestParam Long organizationId
    ) {

        return service.getById(
                id,
                organizationId
        );
    }
    @PutMapping("/{id}")
    public EquipmentResponse update(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @Valid
            @RequestBody
            UpdateEquipmentRequest request
    ) {

        return service.update(
                id,
                organizationId,
                request
        );
    }
    @PatchMapping("/{id}/status")
    public EquipmentResponse changeStatus(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @Valid
            @RequestBody
            UpdateEquipmentStatusRequest request
    ) {

        return service.changeStatus(
                id,
                organizationId,
                request.status()
        );
    }
    @GetMapping("/by-serial/{serial}")
    public EquipmentResponse getBySerial(
            @PathVariable String serial,
            @RequestParam Long organizationId
    ) {

        return service.getBySerial(
                organizationId,
                serial
        );
    }

    @GetMapping("/by-imei/{imei}")
    public EquipmentResponse getByImei(
            @PathVariable String imei,
            @RequestParam Long organizationId
    ) {

        return service.getByImei(
                organizationId,
                imei
        );
    }

    @GetMapping("/by-mac/{mac}")
    public EquipmentResponse getByMac(
            @PathVariable String mac,
            @RequestParam Long organizationId
    ) {

        return service.getByMac(
                organizationId,
                mac
        );
    }
    @GetMapping("/search")
    public List<EquipmentResponse> search(

            @RequestParam Long organizationId,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Long warehouseId,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            Long equipmentTypeId,

            @RequestParam(required = false)
            Long brandId,

            @RequestParam(required = false)
            Long modelId,

            @RequestParam(required = false)
            EquipmentStatus status,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String serialNumber,

            @RequestParam(required = false)
            String imei,

            @RequestParam(required = false)
            String macAddress
    ) {

        return service.search(
                organizationId,
                branchId,
                warehouseId,
                categoryId,
                equipmentTypeId,
                brandId,
                modelId,
                status,
                keyword,
                serialNumber,
                imei,
                macAddress
        );
    }
}