package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.CustomerGroupRequest;
import com.equipmentrental.organizationcustomer.dto.response.CustomerGroupResponse;
import com.equipmentrental.organizationcustomer.service.CustomerGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/customer-groups"
)
@RequiredArgsConstructor
public class CustomerGroupController {

    private final CustomerGroupService groupService;


    @PostMapping
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
    public List<CustomerGroupResponse> getAll(
            @PathVariable Long organizationId
    ) {

        return groupService.getAll(
                organizationId
        );
    }


}
