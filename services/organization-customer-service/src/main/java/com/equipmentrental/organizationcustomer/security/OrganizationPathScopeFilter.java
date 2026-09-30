package com.equipmentrental.organizationcustomer.security;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Enforces the organization id embedded in JWT for every organization-scoped API path. */
@Component
@ConditionalOnBean(CurrentUserProvider.class)
public class OrganizationPathScopeFilter extends OncePerRequestFilter {
    private static final String PREFIX = "/api/v1/organizations/";
    private final CurrentUserProvider currentUserProvider;
    private final DataScopeAuthorizer dataScopeAuthorizer;

    public OrganizationPathScopeFilter(CurrentUserProvider currentUserProvider,
                                       DataScopeAuthorizer dataScopeAuthorizer) {
        this.currentUserProvider = currentUserProvider;
        this.dataScopeAuthorizer = dataScopeAuthorizer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Long organizationId = organizationId(request.getRequestURI());
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (organizationId != null && !currentUser.userId().isBlank()
                && !dataScopeAuthorizer.canAccessOrganization(currentUser, organizationId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Không có quyền truy cập organization này\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Long organizationId(String uri) {
        if (uri == null || !uri.startsWith(PREFIX)) return null;
        String remainder = uri.substring(PREFIX.length());
        int slash = remainder.indexOf('/');
        String value = slash < 0 ? remainder : remainder.substring(0, slash);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
