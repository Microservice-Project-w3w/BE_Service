package com.equipmentrental.rental.security;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import org.springframework.stereotype.Component;

/**
 * Applies organization and branch data scope inside the service layer.
 * Controller permissions decide the action; this guard decides the data slice.
 */
@Component
public class RentalDataScopeGuard {
    private final CurrentUserProvider currentUserProvider;
    private final DataScopeAuthorizer dataScopeAuthorizer;

    public RentalDataScopeGuard(CurrentUserProvider currentUserProvider,
                                DataScopeAuthorizer dataScopeAuthorizer) {
        this.currentUserProvider = currentUserProvider;
        this.dataScopeAuthorizer = dataScopeAuthorizer;
    }

    public void requireBranch(Long organizationId, Long branchId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        if (!dataScopeAuthorizer.canAccessBranch(user, organizationId, branchId)) {
            throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
        }
    }
}
