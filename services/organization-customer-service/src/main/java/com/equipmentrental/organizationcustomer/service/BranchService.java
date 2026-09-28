package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.BranchRequest;
import com.equipmentrental.organizationcustomer.dto.response.BranchResponse;
import com.equipmentrental.organizationcustomer.entity.Branch;
import com.equipmentrental.organizationcustomer.enums.BranchStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchService {
    private final BranchRepository branchRepository;
    private final OrganizationService organizationService;

    public BranchResponse create(Long organizationId, BranchRequest request) {
        organizationService.getEntity(organizationId);
        if (branchRepository.existsByOrganizationIdAndBranchCodeAndDeletedAtIsNull(
                organizationId, request.branchCode())) {
            throw new ConflictException("Mã chi nhánh đã tồn tại trong doanh nghiệp");
        }
        try {
            return toResponse(branchRepository.save(Branch.builder()
                    .organizationId(organizationId).branchCode(request.branchCode().trim())
                    .branchName(request.branchName().trim()).email(clean(request.email()))
                    .phone(clean(request.phone())).address(clean(request.address()))
                    .status(request.status() == null ? BranchStatus.ACTIVE : request.status())
                    .createdBy(request.actorUserId()).updatedBy(request.actorUserId()).build()));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Mã chi nhánh đã tồn tại trong doanh nghiệp");
        }
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAll(Long organizationId) {
        organizationService.getEntity(organizationId);
        return branchRepository.findAllByOrganizationIdAndDeletedAtIsNullOrderByIdDesc(organizationId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BranchResponse getById(Long organizationId, Long branchId) {
        return toResponse(getEntity(organizationId, branchId));
    }

    public Branch getEntity(Long organizationId, Long branchId) {
        return branchRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(branchId, organizationId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chi nhánh id = " + branchId
                        + " trong doanh nghiệp id = " + organizationId));
    }

    public BranchResponse update(Long organizationId, Long branchId, BranchRequest request) {
        Branch branch = getEntity(organizationId, branchId);
        if (!branch.getBranchCode().equals(request.branchCode())
                && branchRepository.existsByOrganizationIdAndBranchCodeAndDeletedAtIsNull(
                organizationId, request.branchCode())) {
            throw new ConflictException("Mã chi nhánh đã tồn tại trong doanh nghiệp");
        }
        branch.setBranchCode(request.branchCode().trim());
        branch.setBranchName(request.branchName().trim());
        branch.setEmail(clean(request.email()));
        branch.setPhone(clean(request.phone()));
        branch.setAddress(clean(request.address()));
        if (request.status() != null) {
            branch.setStatus(request.status());
        }
        branch.setUpdatedBy(request.actorUserId());
        try {
            return toResponse(branchRepository.save(branch));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Mã chi nhánh đã tồn tại trong doanh nghiệp");
        }
    }

    private BranchResponse toResponse(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getOrganizationId(), branch.getBranchCode(),
                branch.getBranchName(), branch.getEmail(), branch.getPhone(), branch.getAddress(),
                branch.getStatus(), branch.getCreatedBy(), branch.getUpdatedBy(), branch.getCreatedAt(),
                branch.getUpdatedAt());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
