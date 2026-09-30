package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.RestrictedCustomerService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api35DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(RestrictedCustomerService.class);
        var controller = new RestrictedCustomerController(service);
        controller.check(1L, 2L); verify(service).check(1L, 2L);
    }
}
