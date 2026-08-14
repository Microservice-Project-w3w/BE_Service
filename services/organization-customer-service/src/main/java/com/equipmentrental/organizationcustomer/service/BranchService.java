package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.BranchRequest;
import com.equipmentrental.organizationcustomer.dto.response.BranchResponse;
import com.equipmentrental.organizationcustomer.entity.Branch;
import com.equipmentrental.organizationcustomer.enums.BranchStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.BranchRepository;
import com.equipmentrental.organizationcustomer.security.OrganizationDataScopeGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchService {

    private final BranchRepository branchRepository;

    private final OrganizationService organizationService;

    private final OrganizationDataScopeGuard dataScopeGuard;


    // =====================================================
    // 1. TẠO CHI NHÁNH
    // =====================================================

    public BranchResponse create(
            Long organizationId,
            BranchRequest request
    ) {

        // Kiểm tra doanh nghiệp có tồn tại hay không
        organizationService.getEntity(organizationId);

        // Kiểm tra mã chi nhánh có bị trùng
        // trong cùng một doanh nghiệp hay không
        if (branchRepository
                .existsByOrganizationIdAndBranchCodeAndDeletedAtIsNull(
                        organizationId,
                        request.branchCode()
                )) {

            throw new ConflictException(
                    "Mã chi nhánh đã tồn tại trong doanh nghiệp"
            );
        }

        Branch branch = Branch.builder()
                .organizationId(organizationId)
                .branchCode(request.branchCode())
                .branchName(request.branchName())
                .email(clean(request.email()))
                .phone(clean(request.phone()))
                .address(clean(request.address()))
                .status(
                        request.status() == null
                                ? BranchStatus.ACTIVE
                                : request.status()
                )
                .createdBy(dataScopeGuard.currentUserId())
                .updatedBy(dataScopeGuard.currentUserId())
                .build();

        Branch saved = branchRepository.save(branch);

        return toResponse(saved);
    }


    // =====================================================
    // 2. DANH SÁCH CHI NHÁNH THEO DOANH NGHIỆP
    // =====================================================

    @Transactional(readOnly = true)
    public List<BranchResponse> getAll(
            Long organizationId
    ) {

        // Đảm bảo organization có tồn tại
        organizationService.getEntity(organizationId);

        return branchRepository
                .findAllByOrganizationIdAndDeletedAtIsNullOrderByIdDesc(
                        organizationId
                )
                .stream()
                .filter(branch -> dataScopeGuard.canAccessBranch(organizationId, branch.getId()))
                .map(this::toResponse)
                .toList();
    }


    // =====================================================
    // 3. CHI TIẾT CHI NHÁNH
    // =====================================================

    @Transactional(readOnly = true)
    public BranchResponse getById(
            Long organizationId,
            Long branchId
    ) {

        Branch branch = getEntity(
                organizationId,
                branchId
        );

        return toResponse(branch);
    }


    // =====================================================
    // 4. CẬP NHẬT CHI NHÁNH
    // =====================================================

    public BranchResponse update(
            Long organizationId,
            Long branchId,
            BranchRequest request
    ) {

        Branch branch = getEntity(
                organizationId,
                branchId
        );

        /*
         * Nếu người dùng đổi branchCode
         * thì phải kiểm tra mã mới có bị trùng không.
         */
        if (!branch.getBranchCode()
                .equals(request.branchCode())

                && branchRepository
                .existsByOrganizationIdAndBranchCodeAndDeletedAtIsNull(
                        organizationId,
                        request.branchCode()
                )) {

            throw new ConflictException(
                    "Mã chi nhánh đã tồn tại trong doanh nghiệp"
            );
        }

        branch.setBranchCode(
                request.branchCode()
        );

        branch.setBranchName(
                request.branchName()
        );

        branch.setEmail(
                clean(request.email())
        );

        branch.setPhone(
                clean(request.phone())
        );

        branch.setAddress(
                clean(request.address())
        );

        // Không gửi status thì giữ status cũ
        if (request.status() != null) {
            branch.setStatus(
                    request.status()
            );
        }

        branch.setUpdatedBy(dataScopeGuard.currentUserId());

        Branch saved =
                branchRepository.save(branch);

        return toResponse(saved);
    }


    // =====================================================
    // 5. XÓA MỀM CHI NHÁNH
    // =====================================================

    public void delete(
            Long organizationId,
            Long branchId,
            Long actorUserId
    ) {

        Branch branch = getEntity(
                organizationId,
                branchId
        );

        /*
         * Không DELETE record khỏi DB.
         * Chỉ đánh dấu DELETED.
         */
        branch.setStatus(
                BranchStatus.DELETED
        );

        branch.setDeletedAt(
                LocalDateTime.now()
        );

        branch.setUpdatedBy(dataScopeGuard.currentUserId());

        branchRepository.save(branch);
    }


    // =====================================================
    // 6. LẤY ENTITY NỘI BỘ
    // =====================================================

    public Branch getEntity(
            Long organizationId,
            Long branchId
    ) {

        return branchRepository
                .findByIdAndOrganizationIdAndDeletedAtIsNull(
                        branchId,
                        organizationId
                )
                .orElseThrow(
                        () -> new NotFoundException(
                                "Không tìm thấy chi nhánh id = "
                                        + branchId
                                        + " trong doanh nghiệp id = "
                                        + organizationId
                        )
                );
    }


    // =====================================================
    // 7. ENTITY -> RESPONSE DTO
    // =====================================================

    private BranchResponse toResponse(
            Branch branch
    ) {

        return new BranchResponse(

                branch.getId(),

                branch.getOrganizationId(),

                branch.getBranchCode(),

                branch.getBranchName(),

                branch.getEmail(),

                branch.getPhone(),

                branch.getAddress(),

                branch.getStatus(),

                branch.getCreatedBy(),

                branch.getUpdatedBy(),

                branch.getCreatedAt(),

                branch.getUpdatedAt()
        );
    }


    // =====================================================
    // 8. CHUYỂN STRING RỖNG THÀNH NULL
    // =====================================================

    private String clean(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
