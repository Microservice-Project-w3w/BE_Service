package com.equipmentrental.inventory.security;

import com.equipmentrental.common.security.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryDataScopeGuardTest {
    CurrentUserProvider users = mock(CurrentUserProvider.class);
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    InventoryDataScopeGuard guard = new InventoryDataScopeGuard(users, new DataScopeAuthorizer(), jdbc);
    public record ScopedData(Long organizationId, Long branchId, Long warehouseId) {}

    @BeforeEach void manager() {
        when(users.getCurrentUser()).thenReturn(new CurrentUser("1", "manager", 1L, Set.of(1L), Set.of("MANAGER"), Set.of(), "s"));
    }
    @Test void deniesForeignOrganization() { assertThrows(AccessDeniedException.class, () -> guard.requireScope(2L, 1L)); }
    @Test void deniesForeignBranchEvenInsideOrganization() { assertThrows(AccessDeniedException.class, () -> guard.requireScope(1L, 2L)); }
    @Test void checksPersistedOwnerBeforeUpdateEvenIfRequestClaimsOwnBranch() {
        when(jdbc.queryForList("SELECT * FROM equipment WHERE id = ?", 7L)).thenReturn(List.of(Map.of("organization_id", 1L, "branch_id", 2L)));
        assertThrows(AccessDeniedException.class, () -> guard.checkArguments("EquipmentService", new String[]{"id", "organizationId", "request"}, new Object[]{7L, 1L, new ScopedData(null, 1L, null)}));
    }
    @Test void deniesCrossBranchWarehouseReference() {
        when(jdbc.queryForList("SELECT * FROM warehouses WHERE id = ?", 9L)).thenReturn(List.of(Map.of("organization_id", 1L, "branch_id", 2L)));
        assertThrows(AccessDeniedException.class, () -> guard.checkArguments("EquipmentService", new String[]{"request"}, new Object[]{new ScopedData(1L, 1L, 9L)}));
    }
    @Test void filtersUnscopedListByBothOrganizationAndBranch() {
        assertTrue(guard.canRead(new ScopedData(1L, 1L, null)));
        assertFalse(guard.canRead(new ScopedData(1L, 2L, null)));
        assertFalse(guard.canRead(new ScopedData(2L, 1L, null)));
    }
    @Test void imageDeleteChecksOwningEquipment() {
        when(jdbc.queryForList("SELECT * FROM equipment_images WHERE id = ?", 4L)).thenReturn(List.of(Map.of("equipment_id", 7L)));
        when(jdbc.queryForList("SELECT * FROM equipment WHERE id = ?", 7L)).thenReturn(List.of(Map.of("organization_id", 2L, "branch_id", 3L)));
        assertThrows(AccessDeniedException.class, () -> guard.checkArguments("EquipmentImageService", new String[]{"id"}, new Object[]{4L}));
    }
    @Test void deniesTransfersToUnassignedBranch() {
        when(jdbc.queryForList("SELECT * FROM stock_transfers WHERE id = ?", 3L)).thenReturn(List.of(Map.of("organization_id", 1L, "from_branch_id", 1L, "to_branch_id", 2L)));
        assertThrows(AccessDeniedException.class, () -> guard.checkArguments("StockTransferService", new String[]{"id"}, new Object[]{3L}));
    }
    @Test void adminCanAccessOtherOrganization() {
        when(users.getCurrentUser()).thenReturn(new CurrentUser("1", "admin", null, Set.of(), Set.of("ADMIN"), Set.of(), "s"));
        assertDoesNotThrow(() -> guard.requireScope(2L, 3L));
    }
}
