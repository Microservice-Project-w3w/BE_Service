package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.organizationcustomer.dto.request.CustomerGroupRequest;
import com.equipmentrental.organizationcustomer.dto.response.CustomerGroupMemberResponse;
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


    // =====================================================
    // 4. CẬP NHẬT NHÓM
    // =====================================================

    public CustomerGroupResponse update(
            Long organizationId,
            Long groupId,
            CustomerGroupRequest request
    ) {

        CustomerGroup group =
                getEntity(organizationId, groupId);

        if (!group.getGroupCode()
                .equals(request.groupCode())

                && groupRepository
                .existsByOrganizationIdAndGroupCodeAndDeletedAtIsNull(
                        organizationId,
                        request.groupCode()
                )) {

            throw new ConflictException(
                    "Mã nhóm khách hàng đã tồn tại"
            );
        }

        group.setGroupCode(
                request.groupCode().trim()
        );

        group.setGroupName(
                request.groupName().trim()
        );

        group.setDescription(
                clean(request.description())
        );

        if (request.status() != null) {
            group.setStatus(request.status());
        }

        group.setUpdatedBy(
                request.actorUserId()
        );

        return toResponse(
                groupRepository.save(group)
        );
    }


    // =====================================================
    // 5. XÓA MỀM NHÓM
    // =====================================================

    public void delete(
            Long organizationId,
            Long groupId,
            Long actorUserId
    ) {

        CustomerGroup group =
                getEntity(organizationId, groupId);

        group.setStatus(
                CustomerGroupStatus.DELETED
        );

        group.setDeletedAt(
                LocalDateTime.now()
        );

        group.setUpdatedBy(actorUserId);

        groupRepository.save(group);
    }


    // =====================================================
    // 6. THÊM KHÁCH HÀNG VÀO NHÓM
    // =====================================================

    public CustomerGroupMemberResponse addMember(
            Long organizationId,
            Long groupId,
            Long customerId,
            Long actorUserId
    ) {

        getEntity(organizationId, groupId);

        // Customer cũng phải thuộc Organization này
        customerService.getEntity(
                organizationId,
                customerId
        );

        if (memberRepository
                .existsByOrganizationIdAndCustomerGroupIdAndCustomerId(
                        organizationId,
                        groupId,
                        customerId
                )) {

            throw new ConflictException(
                    "Khách hàng đã thuộc nhóm này"
            );
        }

        CustomerGroupMember member =
                CustomerGroupMember.builder()
                        .organizationId(organizationId)
                        .customerGroupId(groupId)
                        .customerId(customerId)
                        .createdBy(actorUserId)
                        .build();

        return memberToResponse(
                memberRepository.save(member)
        );
    }


    // =====================================================
    // 7. DANH SÁCH THÀNH VIÊN
    // =====================================================

    @Transactional(readOnly = true)
    public List<CustomerGroupMemberResponse> getMembers(
            Long organizationId,
            Long groupId
    ) {

        getEntity(organizationId, groupId);

        return memberRepository
                .findAllByOrganizationIdAndCustomerGroupIdOrderByIdDesc(
                        organizationId,
                        groupId
                )
                .stream()
                .map(this::memberToResponse)
                .toList();
    }


    // =====================================================
    // 8. XÓA KHÁCH HÀNG KHỎI NHÓM
    // =====================================================

    public void removeMember(
            Long organizationId,
            Long groupId,
            Long customerId
    ) {

        getEntity(organizationId, groupId);

        CustomerGroupMember member =
                memberRepository
                        .findByOrganizationIdAndCustomerGroupIdAndCustomerId(
                                organizationId,
                                groupId,
                                customerId
                        )
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "Khách hàng không thuộc nhóm này"
                                )
                        );

        memberRepository.delete(member);
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


    private CustomerGroupMemberResponse memberToResponse(
            CustomerGroupMember member
    ) {

        return new CustomerGroupMemberResponse(
                member.getId(),
                member.getOrganizationId(),
                member.getCustomerGroupId(),
                member.getCustomerId(),
                member.getCreatedBy(),
                member.getCreatedAt()
        );
    }


    private String clean(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}