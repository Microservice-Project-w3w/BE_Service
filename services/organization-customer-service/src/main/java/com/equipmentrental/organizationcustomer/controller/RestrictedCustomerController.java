package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.RemoveRestrictionRequest;
import com.equipmentrental.organizationcustomer.dto.request.RestrictedCustomerRequest;
import com.equipmentrental.organizationcustomer.dto.response.RestrictedCustomerResponse;
import com.equipmentrental.organizationcustomer.dto.response.RestrictionCheckResponse;
import com.equipmentrental.organizationcustomer.service.RestrictedCustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/restricted-customers"
)
@RequiredArgsConstructor
public class RestrictedCustomerController {

    private final RestrictedCustomerService service;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestrictedCustomerResponse create(
            @PathVariable Long organizationId,
            @Valid
            @RequestBody RestrictedCustomerRequest request
    ) {

        return service.create(
                organizationId,
                request
        );
    }


    @GetMapping
    public List<RestrictedCustomerResponse> getAll(
            @PathVariable Long organizationId,
            @RequestParam(required = false)
            Long customerId
    ) {

        return service.getAll(
                organizationId,
                customerId
        );
    }


    @GetMapping("/check/{customerId}")
    public RestrictionCheckResponse check(
            @PathVariable Long organizationId,
            @PathVariable Long customerId
    ) {

        return service.check(
                organizationId,
                customerId
        );
    }


    @PatchMapping("/{restrictionId}/remove")
    public RestrictedCustomerResponse remove(
            @PathVariable Long organizationId,
            @PathVariable Long restrictionId,
            @Valid
            @RequestBody RemoveRestrictionRequest request
    ) {

        return service.remove(
                organizationId,
                restrictionId,
                request
        );
    }
}