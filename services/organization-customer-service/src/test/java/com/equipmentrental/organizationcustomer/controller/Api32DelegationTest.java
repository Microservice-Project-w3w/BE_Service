package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.CustomerGroupService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api32DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(CustomerGroupService.class);
        var controller = new CustomerGroupController(service);
        controller.removeMember(1L, 2L, 3L); verify(service).removeMember(1L, 2L, 3L);
    }
}
