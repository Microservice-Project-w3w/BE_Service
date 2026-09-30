package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.RestrictedCustomerService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api33DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(RestrictedCustomerService.class);
        var controller = new RestrictedCustomerController(service);
        var request = mock(RestrictedCustomerRequest.class); controller.create(1L, request); verify(service).create(1L, request);
    }
}
