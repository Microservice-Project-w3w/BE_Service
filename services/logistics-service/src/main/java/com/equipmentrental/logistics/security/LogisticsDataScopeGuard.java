package com.equipmentrental.logistics.security;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class LogisticsDataScopeGuard {
    private final CurrentUserProvider currentUserProvider;
    private final DataScopeAuthorizer dataScopeAuthorizer;

    public LogisticsDataScopeGuard(
            CurrentUserProvider currentUserProvider, DataScopeAuthorizer dataScopeAuthorizer) {
        this.currentUserProvider = currentUserProvider;
        this.dataScopeAuthorizer = dataScopeAuthorizer;
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

    public void requireCustomer(Long organizationId, Long branchId, Long customerId) {
        if (!canAccessCustomer(organizationId, branchId, customerId)) {
            throw denied();
        }
    }

    public boolean canAccessBranch(Long organizationId, Long branchId) {
        if (isAdmin(currentUserProvider.getCurrentUser())) {
            return true;
        }
        return dataScopeAuthorizer.canAccessBranch(currentUserProvider.getCurrentUser(), organizationId, branchId);
    }

    public boolean canAccessOrganizationOrBranch(Long organizationId, Long branchId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return branchId == null
                ? dataScopeAuthorizer.canAccessOrganization(user, organizationId)
                : dataScopeAuthorizer.canAccessBranch(user, organizationId, branchId);
    }

    public boolean canAccessCustomer(Long organizationId, Long branchId, Long customerId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        if (!canAccessBranch(organizationId, branchId)) {
            return false;
        }
        return !isCustomer(user) || (customerId != null && customerId.equals(jwtCustomerId()));
    }

    public Long currentUserId() {
        return CurrentUserProvider.toLong(currentUserProvider.getCurrentUser().userId());
    }

    private boolean isCustomer(CurrentUser user) {
        return user.roles().contains("CUSTOMER") || user.roles().contains("ROLE_CUSTOMER");
    }

    private boolean isAdmin(CurrentUser user) {
        return user.roles().contains("ADMIN") || user.roles().contains("ROLE_ADMIN");
    }

    private Long jwtCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return CurrentUserProvider.toLong(jwt.getClaim("customerId"));
    }

    private AccessDeniedException denied() {
        return new AccessDeniedException("Không có quyền truy cập dữ liệu ngoài phạm vi được cấp");
    }
}
