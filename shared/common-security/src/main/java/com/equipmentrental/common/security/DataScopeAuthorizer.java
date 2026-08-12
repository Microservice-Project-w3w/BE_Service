package com.equipmentrental.common.security;

public class DataScopeAuthorizer {

    public boolean canAccessOrganization(CurrentUser user, Long organizationId) {
        if (user == null || organizationId == null) {
            return false;
        }
        return isAdmin(user) || organizationId.equals(user.organizationId());
    }

    public boolean canAccessBranch(CurrentUser user, Long organizationId, Long branchId) {
        if (user == null || organizationId == null || branchId == null) {
            return false;
        }
        return isAdmin(user)
                || (canAccessOrganization(user, organizationId)
                        && user.branchIds().contains(branchId));
    }

    public boolean canAccessOwner(CurrentUser user, String ownerUserId) {
        if (user == null || ownerUserId == null || ownerUserId.isBlank()) {
            return false;
        }
        return isAdmin(user) || ownerUserId.equals(user.userId());
    }

    private boolean isAdmin(CurrentUser user) {
        return user.roles().contains("ADMIN") || user.roles().contains("ROLE_ADMIN");
    }
}
