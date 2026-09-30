package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CreateWarehouseRequest;
import com.equipmentrental.inventory.dto.request.UpdateWarehouseRequest;
import com.equipmentrental.inventory.dto.response.WarehouseResponse;
import com.equipmentrental.inventory.entity.Warehouse;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;

    @Override
    @Transactional
    public WarehouseResponse create(
            CreateWarehouseRequest request
    ) {

        Warehouse warehouse = Warehouse.builder()
                .organizationId(request.organizationId())
                .branchId(request.branchId())
                .code(request.code().trim().toUpperCase())
                .name(request.name().trim())
                .address(request.address())
                .active(
                        request.active() == null
                                ? true
                                : request.active()
                )
                .build();

        Warehouse saved =
                repository.save(warehouse);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> findAll(
            Long organizationId,
            Long branchId
    ) {

        List<Warehouse> warehouses;

        if (organizationId != null && branchId != null) {

            warehouses =
                    repository.findByOrganizationIdAndBranchId(
                            organizationId,
                            branchId
                    );

        } else if (organizationId != null) {

            warehouses =
                    repository.findByOrganizationId(
                            organizationId
                    );

        } else {

            warehouses =
                    repository.findAll();
        }

        return warehouses
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse findById(
            Long id
    ) {

        Warehouse warehouse =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Warehouse not found"
                                )
                        );

        return toResponse(warehouse);
    }

    @Override
    @Transactional
    public WarehouseResponse update(
            Long id,
            UpdateWarehouseRequest request
    ) {

        Warehouse warehouse =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Warehouse not found"
                                )
                        );

        if (request.code() != null
                && !request.code().isBlank()) {

            warehouse.setCode(
                    request.code()
                            .trim()
                            .toUpperCase()
            );
        }

        if (request.name() != null
                && !request.name().isBlank()) {

            warehouse.setName(
                    request.name().trim()
            );
        }

        if (request.address() != null) {

            warehouse.setAddress(
                    request.address()
            );
        }

        Warehouse saved =
                repository.save(warehouse);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public WarehouseResponse changeActive(
            Long id,
            boolean active
    ) {

        Warehouse warehouse =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Warehouse not found"
                                )
                        );

        warehouse.setActive(active);

        Warehouse saved =
                repository.save(warehouse);

        return toResponse(saved);
    }

    private WarehouseResponse toResponse(
            Warehouse warehouse
    ) {

        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getOrganizationId(),
                warehouse.getBranchId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getAddress(),
                warehouse.getActive(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt()
        );
    }
}