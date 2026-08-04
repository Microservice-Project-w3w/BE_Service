package com.equipmentrental.common.security;

import java.util.Set;

public record CurrentUser(
        Long userId,
        String username,
        Set<String> roles,
        Set<String> permissions
) {
}
