package com.equipmentrental.organizationcustomer.security;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component("organizationScope")
public class OrganizationDataScopeGuard {
    private final CurrentUserProvider currentUserProvider;
    private final DataScopeAuthorizer dataScopeAuthorizer;

    public OrganizationDataScopeGuard(
            CurrentUserProvider currentUserProvider, DataScopeAuthorizer dataScopeAuthorizer) {
        this.currentUserProvider = currentUserProvider;
        this.dataScopeAuthorizer = dataScopeAuthorizer;
    }

    public boolean canAccessOrganization(Long organizationId) {
        return dataScopeAuthorizer.canAccessOrganization(currentUserProvider.getCurrentUser(), organizationId);
    }

    public boolean canAccessBranch(Long organizationId, Long branchId) {
        return dataScopeAuthorizer.canAccessBranch(currentUserProvider.getCurrentUser(), organizationId, branchId);
    }

    public boolean canAccessOrganizationOrBranch(Long organizationId, Long branchId) {
        return branchId == null ? isAdmin() : canAccessBranch(organizationId, branchId);
    }

    public void requireOrganization(Long organizationId) {
        if (!canAccessOrganization(organizationId)) {
            throw denied();
        }
    }

    public void requireBranch(Long organizationId, Long branchId) {
        if (!canAccessBranch(organizationId, branchId)) {
            throw denied();
        }
    }

    public void requireOrganizationOrBranch(Long organizationId, Long branchId) {
        if (!canAccessOrganizationOrBranch(organizationId, branchId)) {
            throw denied();
        }
    }

    public Set<Long> branchIds() {
        return currentUserProvider.getCurrentUser().branchIds();
    }

    public boolean isAdmin() {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return user.roles().contains("ADMIN") || user.roles().contains("ROLE_ADMIN");
    }

    public Long currentUserId() {
        return CurrentUserProvider.toLong(currentUserProvider.getCurrentUser().userId());
    }

    private AccessDeniedException denied() {
        return new AccessDeniedException("Không có quyền truy cập dữ liệu ngoài phạm vi được cấp");
    }
}
