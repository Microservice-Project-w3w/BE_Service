package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.CustomerService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api24DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(CustomerService.class);
        var controller = new CustomerController(service);
        controller.checkOwnership(1L, 2L, 9L); verify(service).checkOwnership(1L, 2L, 9L);
    }
}
