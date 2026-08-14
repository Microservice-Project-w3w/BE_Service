package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.OrganizationRequest;
import com.equipmentrental.organizationcustomer.dto.response.OrganizationResponse;
import com.equipmentrental.organizationcustomer.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService service;


    // =====================================================
    // 1. TẠO DOANH NGHIỆP
    // POST /api/v1/organizations
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('organization.profile.update')")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse create(
            @Valid @RequestBody OrganizationRequest request
    ) {
        return service.create(request);
    }


    // =====================================================
    // 2. DANH SÁCH DOANH NGHIỆP
    // GET /api/v1/organizations
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAuthority('organization.profile.read')")
    public List<OrganizationResponse> getAll() {
        return service.getAll();
    }


    // =====================================================
    // 3. CHI TIẾT DOANH NGHIỆP
    // GET /api/v1/organizations/{id}
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.profile.read') and @organizationScope.canAccessOrganization(#id)")
    public OrganizationResponse getById(
            @PathVariable Long id
    ) {
        return service.getById(id);
    }


    // =====================================================
    // 4. CẬP NHẬT DOANH NGHIỆP
    // PUT /api/v1/organizations/{id}
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.profile.update') and @organizationScope.canAccessOrganization(#id)")
    public OrganizationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequest request
    ) {
        return service.update(id, request);
    }


    // =====================================================
    // 5. XÓA MỀM DOANH NGHIỆP
    // DELETE /api/v1/organizations/{id}?actorUserId=1
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.profile.update') and @organizationScope.canAccessOrganization(#id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @RequestParam(required = false) Long actorUserId
    ) {
        service.delete(id, actorUserId);
    }
}
