package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.EmployeeBranchAssignmentService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api17DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(EmployeeBranchAssignmentService.class);
        var controller = new EmployeeBranchAssignmentController(service);
        controller.getAll(1L, 2L); verify(service).getAll(1L, 2L);
    }
}
