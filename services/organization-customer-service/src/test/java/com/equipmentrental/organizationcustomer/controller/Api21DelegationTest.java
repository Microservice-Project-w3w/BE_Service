package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.CustomerService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api21DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(CustomerService.class);
        var controller = new CustomerController(service);
        controller.getById(1L, 2L); verify(service).getById(1L, 2L);
    }
}
