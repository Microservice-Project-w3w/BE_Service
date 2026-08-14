package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.OrganizationRequest;
import com.equipmentrental.organizationcustomer.dto.response.OrganizationResponse;
import com.equipmentrental.organizationcustomer.entity.Organization;
import com.equipmentrental.organizationcustomer.enums.OrganizationStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.OrganizationRepository;
import com.equipmentrental.organizationcustomer.security.OrganizationDataScopeGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository repository;

    private final OrganizationDataScopeGuard dataScopeGuard;


    // =====================================================
    // 1. TẠO DOANH NGHIỆP
    // =====================================================

    public OrganizationResponse create(OrganizationRequest request) {

        // Kiểm tra trùng mã doanh nghiệp
        if (repository.existsByOrganizationCodeAndDeletedAtIsNull(
                request.organizationCode())) {

            throw new ConflictException(
                    "Mã doanh nghiệp đã tồn tại"
            );
        }

        // Kiểm tra trùng mã số thuế
        if (request.taxCode() != null
                && !request.taxCode().isBlank()
                && repository.existsByTaxCodeAndDeletedAtIsNull(
                request.taxCode())) {

            throw new ConflictException(
                    "Mã số thuế đã tồn tại"
            );
        }

        Organization organization = Organization.builder()
                .organizationCode(
                        request.organizationCode()
                )
                .organizationName(
                        request.organizationName()
                )
                .taxCode(
                        clean(request.taxCode())
                )
                .email(
                        clean(request.email())
                )
                .phone(
                        clean(request.phone())
                )
                .address(
                        clean(request.address())
                )
                .status(
                        request.status() == null
                                ? OrganizationStatus.ACTIVE
                                : request.status()
                )
                .createdBy(dataScopeGuard.currentUserId())
                .updatedBy(dataScopeGuard.currentUserId())
                .build();

        Organization saved =
                repository.save(organization);

        return toResponse(saved);
    }


    // =====================================================
    // 2. DANH SÁCH DOANH NGHIỆP
    // =====================================================

    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAll() {

        return repository
                .findAllByDeletedAtIsNullOrderByIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =====================================================
    // 3. CHI TIẾT DOANH NGHIỆP
    // =====================================================

    @Transactional(readOnly = true)
    public OrganizationResponse getById(Long id) {

        Organization organization =
                getEntity(id);

        return toResponse(organization);
    }


    // =====================================================
    // 4. CẬP NHẬT DOANH NGHIỆP
    // =====================================================

    public OrganizationResponse update(
            Long id,
            OrganizationRequest request
    ) {

        Organization organization =
                getEntity(id);


        // Nếu đổi organizationCode thì kiểm tra trùng
        if (!organization
                .getOrganizationCode()
                .equals(request.organizationCode())

                && repository
                .existsByOrganizationCodeAndDeletedAtIsNull(
                        request.organizationCode())) {

            throw new ConflictException(
                    "Mã doanh nghiệp đã tồn tại"
            );
        }


        // Nếu đổi taxCode thì kiểm tra trùng
        if (request.taxCode() != null
                && !request.taxCode().isBlank()

                && !request.taxCode()
                .equals(organization.getTaxCode())

                && repository
                .existsByTaxCodeAndDeletedAtIsNull(
                        request.taxCode())) {

            throw new ConflictException(
                    "Mã số thuế đã tồn tại"
            );
        }


        organization.setOrganizationCode(
                request.organizationCode()
        );

        organization.setOrganizationName(
                request.organizationName()
        );

        organization.setTaxCode(
                clean(request.taxCode())
        );

        organization.setEmail(
                clean(request.email())
        );

        organization.setPhone(
                clean(request.phone())
        );

        organization.setAddress(
                clean(request.address())
        );


        // Nếu client không gửi status
        // thì giữ nguyên status cũ
        if (request.status() != null) {
            organization.setStatus(
                    request.status()
            );
        }


        organization.setUpdatedBy(dataScopeGuard.currentUserId());


        Organization saved =
                repository.save(organization);

        return toResponse(saved);
    }


    // =====================================================
    // 5. XÓA MỀM DOANH NGHIỆP
    // =====================================================

    public void delete(
            Long id,
            Long actorUserId
    ) {

        Organization organization =
                getEntity(id);

        // Không xóa record thật khỏi database
        organization.setStatus(
                OrganizationStatus.DELETED
        );

        organization.setDeletedAt(
                LocalDateTime.now()
        );

        organization.setUpdatedBy(dataScopeGuard.currentUserId());

        repository.save(organization);
    }


    // =====================================================
    // 6. HÀM DÙNG NỘI BỘ
    // =====================================================

    public Organization getEntity(Long id) {

        return repository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Không tìm thấy doanh nghiệp id = " + id
                        )
                );
    }


    // =====================================================
    // 7. ENTITY -> RESPONSE DTO
    // =====================================================

    private OrganizationResponse toResponse(
            Organization organization
    ) {

        return new OrganizationResponse(

                organization.getId(),

                organization.getOrganizationCode(),

                organization.getOrganizationName(),

                organization.getTaxCode(),

                organization.getEmail(),

                organization.getPhone(),

                organization.getAddress(),

                organization.getStatus(),

                organization.getCreatedBy(),

                organization.getUpdatedBy(),

                organization.getCreatedAt(),

                organization.getUpdatedAt()
        );
    }


    // =====================================================
    // 8. CHUYỂN CHUỖI RỖNG -> NULL
    // =====================================================

    private String clean(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
