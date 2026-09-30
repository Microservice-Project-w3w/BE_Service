package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.request.*;
import com.equipmentrental.organizationcustomer.enums.*;
import com.equipmentrental.organizationcustomer.service.RestrictedCustomerService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class Api36DelegationTest {
    @Test
    void delegatesToServiceWithOrganizationScope() {
        var service = mock(RestrictedCustomerService.class);
        var controller = new RestrictedCustomerController(service);
        var request = mock(RemoveRestrictionRequest.class); controller.remove(1L, 2L, request); verify(service).remove(1L, 2L, request);
    }
}
