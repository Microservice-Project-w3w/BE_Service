package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.CustomerService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api23DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(CustomerService.class);
        var controller = new CustomerController(service);
        controller.delete(1L, 2L, 7L); verify(service).delete(1L, 2L, 7L);
    }
}
