package com.equipmentrental.rental.security;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import org.springframework.stereotype.Component;
import com.equipmentrental.rental.client.CustomerPortalClient;

/**
 * Applies organization and branch data scope inside the service layer.
 * Controller permissions decide the action; this guard decides the data slice.
 */
@Component
public class RentalDataScopeGuard {
    private final CurrentUserProvider currentUserProvider;
    private final DataScopeAuthorizer dataScopeAuthorizer;
    private final CustomerPortalClient portal;

    public RentalDataScopeGuard(CurrentUserProvider currentUserProvider, DataScopeAuthorizer dataScopeAuthorizer,
            CustomerPortalClient portal) {
        this.currentUserProvider = currentUserProvider;
        this.dataScopeAuthorizer = dataScopeAuthorizer;
        this.portal = portal;
    }

    public void requireBranch(Long organizationId, Long branchId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        if (!dataScopeAuthorizer.canAccessBranch(user, organizationId, branchId)) {
            throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
        }
        if (isCustomer(user)) customerContext(user, organizationId, branchId);
    }

    public void requireRentalAccess(Long organizationId, Long branchId, Long customerId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        boolean allowed = dataScopeAuthorizer.canAccessBranch(user, organizationId, branchId);
        if (allowed && isCustomer(user))
            allowed = customerId != null && customerId.equals(customerContext(user, organizationId, branchId).customerId());
        if (!allowed) {
            throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
        }
    }

    public Long customerIdForOwnList(Long organizationId, Long branchId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        if (!isCustomer(user)) {
            requireBranch(organizationId, branchId);
            return null;
        }
        return customerContext(user, organizationId, branchId).customerId();
    }

    private boolean isCustomer(CurrentUser user) {
        return user.roles().contains("CUSTOMER") || user.roles().contains("ROLE_CUSTOMER");
    }

    private CustomerPortalClient.Context customerContext(CurrentUser user, Long organizationId, Long branchId) {
        if (!dataScopeAuthorizer.canAccessBranch(user, organizationId, branchId))
            throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
        var context = portal.me(organizationId);
        if (!user.userId().equals(String.valueOf(context.userId())) || !branchId.equals(context.branchId()))
            throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
        return context;
    }
}
