package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.CustomerGroupRequest;
import com.equipmentrental.organizationcustomer.dto.response.CustomerGroupMemberResponse;
import com.equipmentrental.organizationcustomer.dto.response.CustomerGroupResponse;
import com.equipmentrental.organizationcustomer.service.CustomerGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/customer-groups"
)
@RequiredArgsConstructor
public class CustomerGroupController {

    private final CustomerGroupService groupService;


    @PostMapping
    @PreAuthorize("hasAuthority('customer.group.manage') and @organizationScope.canAccessOrganization(#organizationId)")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerGroupResponse create(
            @PathVariable Long organizationId,
            @Valid @RequestBody CustomerGroupRequest request
    ) {

        return groupService.create(
                organizationId,
                request
        );
    }


    @GetMapping
    @PreAuthorize("hasAuthority('customer.group.read') and @organizationScope.canAccessOrganization(#organizationId)")
    public List<CustomerGroupResponse> getAll(
            @PathVariable Long organizationId
    ) {

        return groupService.getAll(
                organizationId
        );
    }


    @GetMapping("/{groupId}")
    @PreAuthorize("hasAuthority('customer.group.read') and @organizationScope.canAccessOrganization(#organizationId)")
    public CustomerGroupResponse getById(
            @PathVariable Long organizationId,
            @PathVariable Long groupId
    ) {

        return groupService.getById(
                organizationId,
                groupId
        );
    }


    @PutMapping("/{groupId}")
    @PreAuthorize("hasAuthority('customer.group.manage') and @organizationScope.canAccessOrganization(#organizationId)")
    public CustomerGroupResponse update(
            @PathVariable Long organizationId,
            @PathVariable Long groupId,
            @Valid @RequestBody CustomerGroupRequest request
    ) {

        return groupService.update(
                organizationId,
                groupId,
                request
        );
    }


    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasAuthority('customer.group.manage') and @organizationScope.canAccessOrganization(#organizationId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long organizationId,
            @PathVariable Long groupId,
            @RequestParam(required = false)
            Long actorUserId
    ) {

        groupService.delete(
                organizationId,
                groupId,
                actorUserId
        );
    }


    // =====================================================
    // THÀNH VIÊN NHÓM
    // =====================================================

    @PostMapping("/{groupId}/members/{customerId}")
    @PreAuthorize("hasAuthority('customer.group.manage') and @organizationScope.canAccessOrganization(#organizationId)")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerGroupMemberResponse addMember(
            @PathVariable Long organizationId,
            @PathVariable Long groupId,
            @PathVariable Long customerId,
            @RequestParam(required = false)
            Long actorUserId
    ) {

        return groupService.addMember(
                organizationId,
                groupId,
                customerId,
                actorUserId
        );
    }


    @GetMapping("/{groupId}/members")
    @PreAuthorize("hasAuthority('customer.group.read') and @organizationScope.canAccessOrganization(#organizationId)")
    public List<CustomerGroupMemberResponse> getMembers(
            @PathVariable Long organizationId,
            @PathVariable Long groupId
    ) {

        return groupService.getMembers(
                organizationId,
                groupId
        );
    }


    @DeleteMapping("/{groupId}/members/{customerId}")
    @PreAuthorize("hasAuthority('customer.group.manage') and @organizationScope.canAccessOrganization(#organizationId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable Long organizationId,
            @PathVariable Long groupId,
            @PathVariable Long customerId
    ) {

        groupService.removeMember(
                organizationId,
                groupId,
                customerId
        );
    }
}
