package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.CustomerService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api22DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(CustomerService.class);
        var controller = new CustomerController(service);
        var request = mock(CustomerRequest.class); controller.update(1L, 2L, request); verify(service).update(1L, 2L, request);
    }
}
