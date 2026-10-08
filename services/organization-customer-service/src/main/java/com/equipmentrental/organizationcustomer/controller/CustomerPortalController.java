package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.service.CustomerPortalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/customers")
@RequiredArgsConstructor
public class CustomerPortalController {
    private final CustomerPortalService service;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerPortalService.Context me(@PathVariable Long organizationId) { return service.me(organizationId); }

    public record LinkRequest(@NotNull @Positive Long userId) {}

    @PutMapping("/{customerId}/portal-account")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('identity.user.read') and hasAuthority('customer.profile.update')")
    public CustomerPortalService.Context bind(@PathVariable Long organizationId, @PathVariable Long customerId,
            @Valid @RequestBody LinkRequest request) { return service.bind(organizationId, customerId, request.userId()); }

    @DeleteMapping("/{customerId}/portal-account")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customer.profile.update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unbind(@PathVariable Long organizationId, @PathVariable Long customerId) { service.unbind(organizationId, customerId); }
}
