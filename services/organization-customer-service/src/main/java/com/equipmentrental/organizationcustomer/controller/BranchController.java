package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.BranchRequest;
import com.equipmentrental.organizationcustomer.dto.response.BranchResponse;
import com.equipmentrental.organizationcustomer.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;


    // =====================================================
    // 1. TẠO CHI NHÁNH
    // POST /api/v1/organizations/{organizationId}/branches
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('organization.branch.create') and @organizationScope.canAccessOrganization(#organizationId)")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(
            @PathVariable Long organizationId,
            @Valid @RequestBody BranchRequest request
    ) {
        return branchService.create(
                organizationId,
                request
        );
    }


    // =====================================================
    // 2. DANH SÁCH CHI NHÁNH
    // GET /api/v1/organizations/{organizationId}/branches
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAuthority('organization.branch.read') and @organizationScope.canAccessOrganization(#organizationId)")
    public List<BranchResponse> getAll(
            @PathVariable Long organizationId
    ) {
        return branchService.getAll(
                organizationId
        );
    }


    // =====================================================
    // 3. CHI TIẾT CHI NHÁNH
    // GET /api/v1/organizations/{organizationId}/branches/{branchId}
    // =====================================================

    @GetMapping("/{branchId}")
    @PreAuthorize("hasAuthority('organization.branch.read') and @organizationScope.canAccessBranch(#organizationId, #branchId)")
    public BranchResponse getById(
            @PathVariable Long organizationId,
            @PathVariable Long branchId
    ) {
        return branchService.getById(
                organizationId,
                branchId
        );
    }


    // =====================================================
    // 4. CẬP NHẬT CHI NHÁNH
    // PUT /api/v1/organizations/{organizationId}/branches/{branchId}
    // =====================================================

    @PutMapping("/{branchId}")
    @PreAuthorize("hasAuthority('organization.branch.update') and @organizationScope.canAccessBranch(#organizationId, #branchId)")
    public BranchResponse update(
            @PathVariable Long organizationId,
            @PathVariable Long branchId,
            @Valid @RequestBody BranchRequest request
    ) {
        return branchService.update(
                organizationId,
                branchId,
                request
        );
    }


    // =====================================================
    // 5. XÓA MỀM CHI NHÁNH
    // =====================================================

    @DeleteMapping("/{branchId}")
    @PreAuthorize("hasAuthority('organization.branch.lock') and @organizationScope.canAccessBranch(#organizationId, #branchId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long organizationId,
            @PathVariable Long branchId,
            @RequestParam(required = false) Long actorUserId
    ) {

        branchService.delete(
                organizationId,
                branchId,
                actorUserId
        );
    }
}
