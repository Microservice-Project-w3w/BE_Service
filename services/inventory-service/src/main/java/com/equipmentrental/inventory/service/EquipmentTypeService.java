package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CreateEquipmentTypeRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentTypeRequest;
import com.equipmentrental.inventory.dto.response.EquipmentTypeResponse;
import com.equipmentrental.inventory.entity.EquipmentCategory;
import com.equipmentrental.inventory.entity.EquipmentType;
import com.equipmentrental.inventory.repository.EquipmentCategoryRepository;
import com.equipmentrental.inventory.repository.EquipmentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentTypeService {

    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentCategoryRepository categoryRepository;

    @Transactional
    public EquipmentTypeResponse create(
            CreateEquipmentTypeRequest request
    ) {

        EquipmentCategory category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy nhóm thiết bị"
                        )
                );

        if (!category.getOrganizationId()
                .equals(request.organizationId())) {

            throw new IllegalArgumentException(
                    "Nhóm thiết bị không thuộc organization này"
            );
        }

        String normalizedCode =
                request.code()
                        .trim()
                        .toUpperCase();

        if (equipmentTypeRepository
                .existsByOrganizationIdAndCode(
                        request.organizationId(),
                        normalizedCode
                )) {

            throw new IllegalArgumentException(
                    "Mã loại thiết bị đã tồn tại"
            );
        }

        EquipmentType equipmentType =
                EquipmentType.builder()
                        .organizationId(
                                request.organizationId()
                        )
                        .categoryId(
                                request.categoryId()
                        )
                        .code(normalizedCode)
                        .name(
                                request.name().trim()
                        )
                        .description(
                                request.description()
                        )
                        .active(
                                request.active() == null
                                        ? true
                                        : request.active()
                        )
                        .build();

        return toResponse(
                equipmentTypeRepository.save(equipmentType)
        );
    }

    @Transactional(readOnly = true)
    public List<EquipmentTypeResponse> getAll(
            Long organizationId,
            Long categoryId
    ) {

        List<EquipmentType> equipmentTypes;

        if (categoryId != null) {

            equipmentTypes =
                    equipmentTypeRepository
                            .findByOrganizationIdAndCategoryId(
                                    organizationId,
                                    categoryId
                            );

        } else {

            equipmentTypes =
                    equipmentTypeRepository
                            .findByOrganizationId(
                                    organizationId
                            );
        }

        return equipmentTypes
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentTypeResponse getById(
            Long id,
            Long organizationId
    ) {

        EquipmentType equipmentType =
                equipmentTypeRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy loại thiết bị"
                                )
                        );

        return toResponse(equipmentType);
    }

    @Transactional
    public EquipmentTypeResponse update(
            Long id,
            Long organizationId,
            UpdateEquipmentTypeRequest request
    ) {

        EquipmentType equipmentType =
                equipmentTypeRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy loại thiết bị"
                                )
                        );

        EquipmentCategory category =
                categoryRepository
                        .findById(request.categoryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy nhóm thiết bị"
                                )
                        );

        if (!category.getOrganizationId()
                .equals(organizationId)) {

            throw new IllegalArgumentException(
                    "Nhóm thiết bị không thuộc organization này"
            );
        }

        String normalizedCode =
                request.code()
                        .trim()
                        .toUpperCase();

        equipmentTypeRepository
                .findByOrganizationIdAndCode(
                        organizationId,
                        normalizedCode
                )
                .filter(existing ->
                        !existing.getId().equals(id)
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Mã loại thiết bị đã tồn tại"
                    );
                });

        equipmentType.setCategoryId(
                request.categoryId()
        );

        equipmentType.setCode(
                normalizedCode
        );

        equipmentType.setName(
                request.name().trim()
        );

        equipmentType.setDescription(
                request.description()
        );

        if (request.active() != null) {
            equipmentType.setActive(
                    request.active()
            );
        }

        return toResponse(
                equipmentTypeRepository.save(
                        equipmentType
                )
        );
    }

    @Transactional
    public EquipmentTypeResponse changeActive(
            Long id,
            Long organizationId,
            boolean active
    ) {

        EquipmentType equipmentType =
                equipmentTypeRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy loại thiết bị"
                                )
                        );

        equipmentType.setActive(active);

        return toResponse(
                equipmentTypeRepository.save(
                        equipmentType
                )
        );
    }

    private EquipmentTypeResponse toResponse(
            EquipmentType equipmentType
    ) {

        return new EquipmentTypeResponse(
                equipmentType.getId(),
                equipmentType.getOrganizationId(),
                equipmentType.getCategoryId(),
                equipmentType.getCode(),
                equipmentType.getName(),
                equipmentType.getDescription(),
                equipmentType.getActive(),
                equipmentType.getCreatedAt(),
                equipmentType.getUpdatedAt()
        );
    }
}