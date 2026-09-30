package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CreateEquipmentModelRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentModelRequest;
import com.equipmentrental.inventory.dto.response.EquipmentModelResponse;
import com.equipmentrental.inventory.entity.Brand;
import com.equipmentrental.inventory.entity.EquipmentModel;
import com.equipmentrental.inventory.entity.EquipmentType;
import com.equipmentrental.inventory.repository.BrandRepository;
import com.equipmentrental.inventory.repository.EquipmentModelRepository;
import com.equipmentrental.inventory.repository.EquipmentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentModelService {

    private final EquipmentModelRepository modelRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public EquipmentModelResponse create(
            CreateEquipmentModelRequest request
    ) {

        EquipmentType equipmentType =
                equipmentTypeRepository
                        .findByIdAndOrganizationId(
                                request.equipmentTypeId(),
                                request.organizationId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy loại thiết bị"
                                )
                        );

        Brand brand =
                brandRepository
                        .findByIdAndOrganizationId(
                                request.brandId(),
                                request.organizationId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy thương hiệu"
                                )
                        );

        String normalizedCode =
                request.code()
                        .trim()
                        .toUpperCase();

        if (modelRepository
                .existsByOrganizationIdAndCode(
                        request.organizationId(),
                        normalizedCode
                )) {

            throw new IllegalArgumentException(
                    "Mã model đã tồn tại"
            );
        }

        EquipmentModel model =
                EquipmentModel.builder()
                        .organizationId(
                                request.organizationId()
                        )
                        .equipmentTypeId(
                                equipmentType.getId()
                        )
                        .brandId(
                                brand.getId()
                        )
                        .code(normalizedCode)
                        .name(
                                request.name().trim()
                        )
                        .manufacturerModel(
                                request.manufacturerModel()
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
                modelRepository.save(model)
        );
    }

    @Transactional(readOnly = true)
    public List<EquipmentModelResponse> getAll(
            Long organizationId,
            Long equipmentTypeId,
            Long brandId
    ) {

        List<EquipmentModel> models;

        if (equipmentTypeId != null && brandId != null) {

            models =
                    modelRepository
                            .findByOrganizationIdAndEquipmentTypeIdAndBrandId(
                                    organizationId,
                                    equipmentTypeId,
                                    brandId
                            );

        } else if (equipmentTypeId != null) {

            models =
                    modelRepository
                            .findByOrganizationIdAndEquipmentTypeId(
                                    organizationId,
                                    equipmentTypeId
                            );

        } else if (brandId != null) {

            models =
                    modelRepository
                            .findByOrganizationIdAndBrandId(
                                    organizationId,
                                    brandId
                            );

        } else {

            models =
                    modelRepository
                            .findByOrganizationId(
                                    organizationId
                            );
        }

        return models
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentModelResponse getById(
            Long id,
            Long organizationId
    ) {

        EquipmentModel model =
                modelRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy model thiết bị"
                                )
                        );

        return toResponse(model);
    }

    @Transactional
    public EquipmentModelResponse update(
            Long id,
            Long organizationId,
            UpdateEquipmentModelRequest request
    ) {

        EquipmentModel model =
                modelRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy model thiết bị"
                                )
                        );

        equipmentTypeRepository
                .findByIdAndOrganizationId(
                        request.equipmentTypeId(),
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy loại thiết bị"
                        )
                );

        brandRepository
                .findByIdAndOrganizationId(
                        request.brandId(),
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy thương hiệu"
                        )
                );

        String normalizedCode =
                request.code()
                        .trim()
                        .toUpperCase();

        modelRepository
                .findByOrganizationIdAndCode(
                        organizationId,
                        normalizedCode
                )
                .filter(existing ->
                        !existing.getId().equals(id)
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Mã model đã tồn tại"
                    );
                });

        model.setEquipmentTypeId(
                request.equipmentTypeId()
        );

        model.setBrandId(
                request.brandId()
        );

        model.setCode(
                normalizedCode
        );

        model.setName(
                request.name().trim()
        );

        model.setManufacturerModel(
                request.manufacturerModel()
        );

        model.setDescription(
                request.description()
        );

        if (request.active() != null) {
            model.setActive(
                    request.active()
            );
        }

        return toResponse(
                modelRepository.save(model)
        );
    }

    @Transactional
    public EquipmentModelResponse changeActive(
            Long id,
            Long organizationId,
            boolean active
    ) {

        EquipmentModel model =
                modelRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy model thiết bị"
                                )
                        );

        model.setActive(active);

        return toResponse(
                modelRepository.save(model)
        );
    }

    private EquipmentModelResponse toResponse(
            EquipmentModel model
    ) {

        return new EquipmentModelResponse(
                model.getId(),
                model.getOrganizationId(),
                model.getEquipmentTypeId(),
                model.getBrandId(),
                model.getCode(),
                model.getName(),
                model.getManufacturerModel(),
                model.getDescription(),
                model.getActive(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}