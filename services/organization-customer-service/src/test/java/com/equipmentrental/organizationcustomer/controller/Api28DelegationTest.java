package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.CustomerGroupService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api28DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(CustomerGroupService.class);
        var controller = new CustomerGroupController(service);
        var request = mock(CustomerGroupRequest.class); controller.update(1L, 2L, request); verify(service).update(1L, 2L, request);
    }
}
