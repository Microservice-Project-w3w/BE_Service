package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.OrganizationRequest;
import com.equipmentrental.organizationcustomer.dto.response.OrganizationResponse;
import com.equipmentrental.organizationcustomer.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse create(@Valid @RequestBody OrganizationRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<OrganizationResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public OrganizationResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public OrganizationResponse update(@PathVariable Long id,
                                       @Valid @RequestBody OrganizationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestParam(required = false) Long actorUserId) {
        service.delete(id, actorUserId);
    }
}
