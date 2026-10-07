package com.equipmentrental.identity.service;

import com.equipmentrental.identity.dto.request.AdminUpdateUserRequest;
import com.equipmentrental.identity.entity.*;
import com.equipmentrental.identity.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdentityManagementServiceTest {
    UserRepository users = mock(UserRepository.class);
    RoleRepository roles = mock(RoleRepository.class);
    SessionService sessions = mock(SessionService.class);
    IdentityManagementService service = new IdentityManagementService(users, roles, mock(PasswordEncoder.class), sessions, mock(AuditLogService.class));
    Role sales = new Role("SALES_STAFF", "Sales", "", true, true);
    User target() {
        User user = new User(); user.setRole(sales); user.setFullName("Old"); user.setEmail("old@example.com");
        user.setStatus(UserStatus.ACTIVE); user.setOrganizationId(1L); user.setBranchIds(Set.of(1L));
        when(users.findDetailedById(1L)).thenReturn(Optional.of(user));
        when(users.findDetailedById(2L)).thenReturn(Optional.of(new User()));
        when(roles.findByCode("SALES_STAFF")).thenReturn(Optional.of(sales));
        when(users.save(user)).thenReturn(user); return user;
    }
    @Test void savesAllProfileAndScopeFieldsAndRevokesChangedAccess() {
        User user = target();
        service.update(1L, new AdminUpdateUserRequest("New", "new@example.com", "0901234567", "SALES_STAFF", 1L, Set.of(3L), UserStatus.INACTIVE), 2L);
        assertEquals("New", user.getFullName()); assertEquals("new@example.com", user.getEmail()); assertEquals("0901234567", user.getPhone());
        assertEquals(Set.of(3L), user.getBranchIds()); assertEquals(UserStatus.INACTIVE, user.getStatus());
        verify(sessions).revokeAllForUser(1L, "ADMIN_ACCOUNT_UPDATED");
    }
    @Test void rejectsBusinessAccountWithoutAssignedBranch() {
        assertThrows(ResponseStatusException.class, () -> service.update(1L, new AdminUpdateUserRequest("New", "new@example.com", "", "SALES_STAFF", 1L, Set.of(), UserStatus.ACTIVE), 2L));
        verify(users, never()).save(any());
    }
    @Test void doesNotAllowDeletingThroughUpdateForm() {
        assertThrows(ResponseStatusException.class, () -> service.update(1L, new AdminUpdateUserRequest("New", "new@example.com", "", "SALES_STAFF", 1L, Set.of(1L), UserStatus.DELETED), 2L));
    }
    @Test void preventsSelfDeactivation() {
        target();
        assertThrows(ResponseStatusException.class, () -> service.update(1L, new AdminUpdateUserRequest("Old", "old@example.com", "", "SALES_STAFF", 1L, Set.of(1L), UserStatus.LOCKED), 1L));
    }
}
