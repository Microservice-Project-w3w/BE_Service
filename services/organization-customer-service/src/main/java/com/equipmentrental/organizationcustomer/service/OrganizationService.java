package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.OrganizationRequest;
import com.equipmentrental.organizationcustomer.dto.response.OrganizationResponse;
import com.equipmentrental.organizationcustomer.entity.Organization;
import com.equipmentrental.organizationcustomer.enums.OrganizationStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

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

    @Transactional(readOnly = true)
    public OrganizationResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    public Organization getEntity(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy doanh nghiệp id = " + id));
    }

    public OrganizationResponse update(Long id, OrganizationRequest request) {
        Organization organization = getEntity(id);
        if (!organization.getOrganizationCode().equals(request.organizationCode())
                && repository.existsByOrganizationCodeAndDeletedAtIsNull(request.organizationCode())) {
            throw new ConflictException("Mã doanh nghiệp đã tồn tại");
        }
        String taxCode = clean(request.taxCode());
        if (taxCode != null && !taxCode.equals(organization.getTaxCode())
                && repository.existsByTaxCodeAndDeletedAtIsNull(taxCode)) {
            throw new ConflictException("Mã số thuế đã tồn tại");
        }
        organization.setOrganizationCode(request.organizationCode().trim());
        organization.setOrganizationName(request.organizationName().trim());
        organization.setTaxCode(taxCode);
        organization.setEmail(clean(request.email()));
        organization.setPhone(clean(request.phone()));
        organization.setAddress(clean(request.address()));
        if (request.status() != null) {
            organization.setStatus(request.status());
        }
        organization.setUpdatedBy(request.actorUserId());
        try {
            return toResponse(repository.save(organization));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Mã doanh nghiệp hoặc mã số thuế đã tồn tại");
        }
    }

    public void delete(Long id, Long actorUserId) {
        Organization organization = getEntity(id);
        organization.setStatus(OrganizationStatus.DELETED);
        organization.setDeletedAt(LocalDateTime.now());
        organization.setUpdatedBy(actorUserId);
        repository.save(organization);
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
