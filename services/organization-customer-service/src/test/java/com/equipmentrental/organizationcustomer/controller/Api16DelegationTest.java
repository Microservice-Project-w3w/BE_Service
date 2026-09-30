package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.EmployeeBranchAssignmentService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api16DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(EmployeeBranchAssignmentService.class);
        var controller = new EmployeeBranchAssignmentController(service);
        var request = mock(EmployeeBranchAssignmentRequest.class); controller.create(1L, request); verify(service).create(1L, request);
    }
}
