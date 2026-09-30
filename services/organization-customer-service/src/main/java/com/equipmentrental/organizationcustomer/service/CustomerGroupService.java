package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.CustomerGroupRequest;
import com.equipmentrental.organizationcustomer.dto.response.CustomerGroupResponse;
import com.equipmentrental.organizationcustomer.entity.CustomerGroup;
import com.equipmentrental.organizationcustomer.entity.CustomerGroupMember;
import com.equipmentrental.organizationcustomer.enums.CustomerGroupStatus;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.CustomerGroupMemberRepository;
import com.equipmentrental.organizationcustomer.repository.CustomerGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerGroupService {

    private final CustomerGroupRepository groupRepository;

    private final CustomerGroupMemberRepository memberRepository;

    private final OrganizationService organizationService;

    private final CustomerService customerService;


    // =====================================================
    // 1. TẠO NHÓM
    // =====================================================

    public CustomerGroupResponse create(
            Long organizationId,
            CustomerGroupRequest request
    ) {

        organizationService.getEntity(organizationId);

        if (groupRepository
                .existsByOrganizationIdAndGroupCodeAndDeletedAtIsNull(
                        organizationId,
                        request.groupCode()
                )) {

            throw new ConflictException(
                    "Mã nhóm khách hàng đã tồn tại"
            );
        }

        CustomerGroup group = CustomerGroup.builder()
                .organizationId(organizationId)
                .groupCode(request.groupCode().trim())
                .groupName(request.groupName().trim())
                .description(clean(request.description()))
                .status(
                        request.status() == null
                                ? CustomerGroupStatus.ACTIVE
                                : request.status()
                )
                .createdBy(request.actorUserId())
                .updatedBy(request.actorUserId())
                .build();

        return toResponse(
                groupRepository.save(group)
        );
    }


    // =====================================================
    // 2. DANH SÁCH NHÓM
    // =====================================================

    @Transactional(readOnly = true)
    public List<CustomerGroupResponse> getAll(
            Long organizationId
    ) {

        organizationService.getEntity(organizationId);

        return groupRepository
                .findAllByOrganizationIdAndDeletedAtIsNullOrderByIdDesc(
                        organizationId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =====================================================
    // 3. CHI TIẾT NHÓM
    // =====================================================

    @Transactional(readOnly = true)
    public CustomerGroupResponse getById(
            Long organizationId,
            Long groupId
    ) {

        return toResponse(
                getEntity(organizationId, groupId)
        );
    }



    public CustomerGroup getEntity(
            Long organizationId,
            Long groupId
    ) {

        return groupRepository
                .findByIdAndOrganizationIdAndDeletedAtIsNull(
                        groupId,
                        organizationId
                )
                .orElseThrow(
                        () -> new NotFoundException(
                                "Không tìm thấy nhóm khách hàng id = "
                                        + groupId
                        )
                );
    }


    private CustomerGroupResponse toResponse(
            CustomerGroup group
    ) {

        return new CustomerGroupResponse(
                group.getId(),
                group.getOrganizationId(),
                group.getGroupCode(),
                group.getGroupName(),
                group.getDescription(),
                group.getStatus(),
                group.getCreatedBy(),
                group.getUpdatedBy(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }


    private String clean(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
