package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.common.security.*;
import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.organizationcustomer.entity.*;
import com.equipmentrental.organizationcustomer.enums.CustomerStatus;
import com.equipmentrental.organizationcustomer.repository.*;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerPortalServiceTest {
    private final CustomerPortalAccountRepository links = mock(CustomerPortalAccountRepository.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CurrentUserProvider users = mock(CurrentUserProvider.class);
    private final CustomerPortalService service = new CustomerPortalService(links, customers, users,
            new DataScopeAuthorizer(), "http://localhost");
    private final Customer customer = Customer.builder().id(42L).organizationId(1L).branchId(2L)
            .ownerUserId(3L).status(CustomerStatus.ACTIVE).displayName("Khách hàng").build();

    @BeforeEach void setup() {
        when(users.getCurrentUser()).thenReturn(new CurrentUser("5", "customer", 1L, Set.of(2L),
                Set.of("CUSTOMER"), Set.of(), "session"));
        when(links.findById(5L)).thenReturn(Optional.of(new CustomerPortalAccount(5L, 42L)));
        when(customers.findByIdAndOrganizationIdAndDeletedAtIsNull(42L, 1L)).thenReturn(Optional.of(customer));
    }
    @Test void usesSeparatePortalLinkAndKeepsTheSalesOwner() {
        var context = service.me(1L);
        assertEquals(42L, context.customerId());
        assertEquals(5L, context.userId());
        assertEquals(3L, customer.getOwnerUserId());
    }
    @Test void refusesMissingLink() {
        when(links.findById(5L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.me(1L));
        verifyNoInteractions(customers);
    }
    @Test void refusesBlockedOrMovedCustomer() {
        customer.setStatus(CustomerStatus.BLOCKED);
        assertThrows(BusinessException.class, () -> service.me(1L));
        customer.setStatus(CustomerStatus.ACTIVE); customer.setBranchId(3L);
        assertThrows(BusinessException.class, () -> service.me(1L));
    }
    @Test void refusesOtherOrganization() {
        assertThrows(BusinessException.class, () -> service.me(9L));
    }
    @Test void staffCannotUseSelfServiceCustomerContext() {
        when(users.getCurrentUser()).thenReturn(new CurrentUser("5", "staff", 1L, Set.of(2L),
                Set.of("SALES_STAFF"), Set.of(), "session"));
        assertThrows(BusinessException.class, () -> service.me(1L));
        verifyNoInteractions(links);
    }
}
