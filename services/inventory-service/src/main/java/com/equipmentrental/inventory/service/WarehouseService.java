package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CreateWarehouseRequest;
import com.equipmentrental.inventory.dto.request.UpdateWarehouseRequest;
import com.equipmentrental.inventory.dto.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {

    WarehouseResponse create(
            CreateWarehouseRequest request
    );

    List<WarehouseResponse> findAll(
            Long organizationId,
            Long branchId
    );

    WarehouseResponse findById(
            Long id
    );

    WarehouseResponse update(
            Long id,
            UpdateWarehouseRequest request
    );

    WarehouseResponse changeActive(
            Long id,
            boolean active
    );
}