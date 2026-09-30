package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CreateBrandRequest;
import com.equipmentrental.inventory.dto.request.UpdateBrandRequest;
import com.equipmentrental.inventory.dto.response.BrandResponse;
import com.equipmentrental.inventory.entity.Brand;
import com.equipmentrental.inventory.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository repository;

    @Transactional
    public BrandResponse create(
            CreateBrandRequest request
    ) {

        String normalizedCode =
                request.code()
                        .trim()
                        .toUpperCase();

        if (repository.existsByOrganizationIdAndCode(
                request.organizationId(),
                normalizedCode
        )) {
            throw new IllegalArgumentException(
                    "Mã thương hiệu đã tồn tại"
            );
        }

        Brand brand = Brand.builder()
                .organizationId(
                        request.organizationId()
                )
                .code(normalizedCode)
                .name(request.name().trim())
                .description(request.description())
                .active(
                        request.active() == null
                                ? true
                                : request.active()
                )
                .build();

        return toResponse(
                repository.save(brand)
        );
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getAll(
            Long organizationId
    ) {

        return repository
                .findByOrganizationId(organizationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse getById(
            Long id,
            Long organizationId
    ) {

        Brand brand = repository
                .findByIdAndOrganizationId(
                        id,
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy thương hiệu"
                        )
                );

        return toResponse(brand);
    }

    @Transactional
    public BrandResponse update(
            Long id,
            Long organizationId,
            UpdateBrandRequest request
    ) {

        Brand brand = repository
                .findByIdAndOrganizationId(
                        id,
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

        repository.findByOrganizationIdAndCode(
                        organizationId,
                        normalizedCode
                )
                .filter(existing ->
                        !existing.getId().equals(id)
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Mã thương hiệu đã tồn tại"
                    );
                });

        brand.setCode(normalizedCode);
        brand.setName(request.name().trim());
        brand.setDescription(request.description());

        if (request.active() != null) {
            brand.setActive(
                    request.active()
            );
        }

        return toResponse(
                repository.save(brand)
        );
    }

    @Transactional
    public BrandResponse changeActive(
            Long id,
            Long organizationId,
            boolean active
    ) {

        Brand brand = repository
                .findByIdAndOrganizationId(
                        id,
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy thương hiệu"
                        )
                );

        brand.setActive(active);

        return toResponse(
                repository.save(brand)
        );
    }

    private BrandResponse toResponse(
            Brand brand
    ) {

        return new BrandResponse(
                brand.getId(),
                brand.getOrganizationId(),
                brand.getCode(),
                brand.getName(),
                brand.getDescription(),
                brand.getActive(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }
}