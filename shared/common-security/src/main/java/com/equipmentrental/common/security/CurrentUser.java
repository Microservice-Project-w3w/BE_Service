package com.equipmentrental.common.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public record CurrentUser(
        String userId,
        String username,
        Long organizationId,
        Set<Long> branchIds,
        Set<String> roles,
        Set<String> permissions,
        String sessionId) {
    public CurrentUser {
        branchIds = immutableSet(branchIds);
        roles = immutableSet(roles);
        permissions = immutableSet(permissions);
    }

    public static CurrentUser empty() {
        return new CurrentUser(null, null, null, Set.of(), Set.of(), Set.of(), null);
    }

    private static <T> Set<T> immutableSet(Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<T> result = new LinkedHashSet<>();
        for (T value : values) {
            if (value != null) {
                result.add(value);
            }
        }
        return Set.copyOf(result);
    }
}
