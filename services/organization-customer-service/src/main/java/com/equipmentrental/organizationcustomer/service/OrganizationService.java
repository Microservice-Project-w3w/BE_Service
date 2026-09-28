package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.OrganizationRequest;
import com.equipmentrental.organizationcustomer.dto.response.OrganizationResponse;
import com.equipmentrental.organizationcustomer.entity.Organization;
import com.equipmentrental.organizationcustomer.enums.OrganizationStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {
    private final OrganizationRepository repository;

    public OrganizationResponse create(OrganizationRequest request) {
        if (repository.existsByOrganizationCodeAndDeletedAtIsNull(request.organizationCode())) {
            throw new ConflictException("Mã doanh nghiệp đã tồn tại");
        }
        if (request.taxCode() != null && !request.taxCode().isBlank()
                && repository.existsByTaxCodeAndDeletedAtIsNull(request.taxCode())) {
            throw new ConflictException("Mã số thuế đã tồn tại");
        }
        Organization saved;
        try {
            saved = repository.save(Organization.builder()
                    .organizationCode(request.organizationCode().trim())
                    .organizationName(request.organizationName().trim())
                    .taxCode(clean(request.taxCode())).email(clean(request.email()))
                    .phone(clean(request.phone())).address(clean(request.address()))
                    .status(request.status() == null ? OrganizationStatus.ACTIVE : request.status())
                    .createdBy(request.actorUserId()).updatedBy(request.actorUserId()).build());
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Mã doanh nghiệp hoặc mã số thuế đã tồn tại");
        }
        return new OrganizationResponse(saved.getId(), saved.getOrganizationCode(), saved.getOrganizationName(),
                saved.getTaxCode(), saved.getEmail(), saved.getPhone(), saved.getAddress(), saved.getStatus(),
                saved.getCreatedBy(), saved.getUpdatedBy(), saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAll() {
        return repository.findAllByDeletedAtIsNullOrderByIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private OrganizationResponse toResponse(Organization organization) {
        return new OrganizationResponse(organization.getId(), organization.getOrganizationCode(),
                organization.getOrganizationName(), organization.getTaxCode(), organization.getEmail(),
                organization.getPhone(), organization.getAddress(), organization.getStatus(),
                organization.getCreatedBy(), organization.getUpdatedBy(), organization.getCreatedAt(),
                organization.getUpdatedAt());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
