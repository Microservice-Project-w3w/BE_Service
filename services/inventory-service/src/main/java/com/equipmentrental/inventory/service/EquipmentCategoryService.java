package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CreateEquipmentCategoryRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentCategoryRequest;
import com.equipmentrental.inventory.dto.response.EquipmentCategoryResponse;
import com.equipmentrental.inventory.entity.EquipmentCategory;
import com.equipmentrental.inventory.repository.EquipmentCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentCategoryService {

    private final EquipmentCategoryRepository repository;

    @Transactional
    public EquipmentCategoryResponse create(
            CreateEquipmentCategoryRequest request
    ) {

        String normalizedCode = request.code().trim().toUpperCase();

        if (repository.existsByOrganizationIdAndCode(
                request.organizationId(),
                normalizedCode
        )) {
            throw new IllegalArgumentException(
                    "Mã nhóm thiết bị đã tồn tại"
            );
        }

        EquipmentCategory category = EquipmentCategory.builder()
                .organizationId(request.organizationId())
                .code(normalizedCode)
                .name(request.name().trim())
                .description(request.description())
                .active(
                        request.active() == null
                                ? true
                                : request.active()
                )
                .build();

        return toResponse(repository.save(category));
    }

    @Transactional(readOnly = true)
    public List<EquipmentCategoryResponse> getAll(
            Long organizationId
    ) {
        return repository
                .findByOrganizationId(organizationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentCategoryResponse getById(
            Long id,
            Long organizationId
    ) {

        EquipmentCategory category = repository.findById(id)
                .filter(c ->
                        c.getOrganizationId()
                                .equals(organizationId)
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy nhóm thiết bị"
                        )
                );

        return toResponse(category);
    }

    @Transactional
    public EquipmentCategoryResponse update(
            Long id,
            Long organizationId,
            UpdateEquipmentCategoryRequest request
    ) {

        EquipmentCategory category = repository.findById(id)
                .filter(c ->
                        c.getOrganizationId()
                                .equals(organizationId)
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy nhóm thiết bị"
                        )
                );

        String normalizedCode =
                request.code().trim().toUpperCase();

        repository
                .findByOrganizationIdAndCode(
                        organizationId,
                        normalizedCode
                )
                .filter(existing ->
                        !existing.getId().equals(id)
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Mã nhóm thiết bị đã tồn tại"
                    );
                });

        category.setCode(normalizedCode);
        category.setName(request.name().trim());
        category.setDescription(request.description());

        if (request.active() != null) {
            category.setActive(request.active());
        }

        return toResponse(repository.save(category));
    }

    @Transactional
    public EquipmentCategoryResponse changeActive(
            Long id,
            Long organizationId,
            boolean active
    ) {
        EquipmentCategory category = repository.findById(id)
                .filter(c -> c.getOrganizationId().equals(organizationId))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy nhóm thiết bị"
                        )
                );

        category.setActive(active);

        return toResponse(repository.save(category));
    }

    private EquipmentCategoryResponse toResponse(
            EquipmentCategory category
    ) {
        return new EquipmentCategoryResponse(
                category.getId(),
                category.getOrganizationId(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.getActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}