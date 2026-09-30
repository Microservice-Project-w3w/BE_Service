package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.BranchRequest;
import com.equipmentrental.organizationcustomer.dto.response.BranchResponse;
import com.equipmentrental.organizationcustomer.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/branches")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasAuthority('organization.branch.create')")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(@PathVariable Long organizationId,
                                 @Valid @RequestBody BranchRequest request) {
        return branchService.create(organizationId, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('organization.branch.read')")
    public List<BranchResponse> getAll(@PathVariable Long organizationId) {
        return branchService.getAll(organizationId);
    }

    @GetMapping("/{branchId}")
    @PreAuthorize("hasAuthority('organization.branch.read')")
    public BranchResponse getById(@PathVariable Long organizationId,
                                  @PathVariable Long branchId) {
        return branchService.getById(organizationId, branchId);
    }

    @PutMapping("/{branchId}")
    @PreAuthorize("hasAuthority('organization.branch.update')")
    public BranchResponse update(@PathVariable Long organizationId,
                                 @PathVariable Long branchId,
                                 @Valid @RequestBody BranchRequest request) {
        return branchService.update(organizationId, branchId, request);
    }

    @DeleteMapping("/{branchId}")
    @PreAuthorize("hasAuthority('organization.branch.lock')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long organizationId,
                       @PathVariable Long branchId,
                       @RequestParam(required = false) Long actorUserId) {
        branchService.delete(organizationId, branchId, actorUserId);
    }
}
