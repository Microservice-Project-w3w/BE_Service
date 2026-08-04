package com.equipmentrental.common.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class CurrentUserProvider {

    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return CurrentUser.empty();
        }
        return fromJwt(jwt);
    }

    public CurrentUser fromJwt(Jwt jwt) {
        if (jwt == null) {
            return CurrentUser.empty();
        }
        String username = firstNonBlank(
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("username")
        );
        return new CurrentUser(
                jwt.getSubject(),
                username,
                toLong(jwt.getClaim("organizationId")),
                toLongSet(jwt.getClaim("branchIds")),
                toStringSet(jwt.getClaim("roles")),
                toStringSet(jwt.getClaim("permissions")),
                jwt.getClaimAsString("sessionId")
        );
    }

    public static Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.valueOf(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public static Set<Long> toLongSet(Object value) {
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        if (value instanceof Collection<?> values) {
            for (Object item : values) {
                addLong(result, item);
            }
        } else {
            addLong(result, value);
        }
        return Set.copyOf(result);
    }

    public static Set<String> toStringSet(Object value) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (value instanceof Collection<?> values) {
            for (Object item : values) {
                addString(result, item);
            }
        } else {
            addString(result, value);
        }
        return Set.copyOf(result);
    }

    private static void addLong(Set<Long> target, Object value) {
        Long converted = toLong(value);
        if (converted != null) {
            target.add(converted);
        }
    }

    private static void addString(Set<String> target, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (!text.isEmpty()) {
            target.add(text);
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
