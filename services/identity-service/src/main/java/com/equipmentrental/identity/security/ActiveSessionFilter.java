package com.equipmentrental.identity.security;

import com.equipmentrental.identity.service.SessionService;
import com.equipmentrental.identity.repository.UserRepository;
import com.equipmentrental.identity.entity.UserStatus;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/** Checks locally to avoid Identity calling its own /me endpoint recursively. */
public class ActiveSessionFilter extends OncePerRequestFilter {
    private final SessionService sessions;
    private final UserRepository users;
    public ActiveSessionFilter(SessionService sessions, UserRepository users) {
        this.sessions = sessions; this.users = users;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken auth) {
            try {
                Long userId = Long.valueOf(auth.getToken().getSubject());
                Long sessionId = Long.valueOf(auth.getToken().getClaimAsString("sessionId"));
                sessions.requireActiveForUser(userId, sessionId);
                if (users.findById(userId).map(u -> u.getStatus() != UserStatus.ACTIVE).orElse(true))
                    throw new IllegalArgumentException("Inactive user");
            } catch (RuntimeException exception) {
                SecurityContextHolder.clearContext();
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"SESSION_REVOKED\",\"message\":\"Phiên đăng nhập không còn hiệu lực\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
