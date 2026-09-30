package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.EmployeeBranchAssignmentService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api18DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(EmployeeBranchAssignmentService.class);
        var controller = new EmployeeBranchAssignmentController(service);
        controller.deactivate(1L, 3L, 7L); verify(service).deactivate(1L, 3L, 7L);
    }
}
