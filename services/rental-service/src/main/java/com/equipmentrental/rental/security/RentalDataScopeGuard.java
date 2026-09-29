package com.equipmentrental.rental.security;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Applies organization and branch data scope inside the service layer.
 * Controller permissions decide the action; this guard decides the data slice.
 */
@Component
public class RentalDataScopeGuard {
    private final CurrentUserProvider currentUserProvider;
    private final DataScopeAuthorizer dataScopeAuthorizer;

    public RentalDataScopeGuard(CurrentUserProvider currentUserProvider, DataScopeAuthorizer dataScopeAuthorizer) {
        this.currentUserProvider = currentUserProvider;
        this.dataScopeAuthorizer = dataScopeAuthorizer;
    }

    public void requireBranch(Long organizationId, Long branchId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        if (!dataScopeAuthorizer.canAccessBranch(user, organizationId, branchId)) {
            throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
        }
    }

    public void requireRentalAccess(Long organizationId, Long branchId, Long customerId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        boolean allowed = isCustomer(user)
                ? dataScopeAuthorizer.canAccessBranch(user, organizationId, branchId)
                        && customerId != null
                        && customerId.equals(jwtCustomerId())
                : dataScopeAuthorizer.canAccessBranch(user, organizationId, branchId);
        if (!allowed) {
            throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
        }
    }

    public Long customerIdForOwnList(Long organizationId, Long branchId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        requireBranch(organizationId, branchId);
        if (!isCustomer(user)) {
            return null;
        }
        Long customerId = jwtCustomerId();
        if (customerId == null) {
            throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
        }
        return customerId;
    }

    private boolean isCustomer(CurrentUser user) {
        return user.roles().contains("CUSTOMER") || user.roles().contains("ROLE_CUSTOMER");
    }

    private Long jwtCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return CurrentUserProvider.toLong(jwt.getClaim("customerId"));
    }
}
