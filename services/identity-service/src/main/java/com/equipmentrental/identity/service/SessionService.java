package com.equipmentrental.identity.service;

import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import com.equipmentrental.identity.dto.response.SessionResponse;
import com.equipmentrental.identity.entity.User;
import com.equipmentrental.identity.entity.UserSession;
import com.equipmentrental.identity.repository.UserSessionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SessionService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserSessionRepository repository;
    private final long refreshTokenDays;

    public SessionService(
            UserSessionRepository repository, @Value("${security.jwt.refresh-token-days:7}") long refreshTokenDays) {
        this.repository = repository;
        this.refreshTokenDays = refreshTokenDays;
    }

    public IssuedSession create(User user, String deviceName, String deviceType, String ipAddress, String userAgent) {
        String rawToken = randomToken();
        UserSession session = new UserSession(
                user,
                hash(rawToken),
                LocalDateTime.now().plusDays(refreshTokenDays),
                deviceName,
                deviceType,
                ipAddress,
                userAgent);
        return new IssuedSession(repository.save(session), rawToken);
    }

    public IssuedSession rotate(String refreshToken) {
        UserSession previous = repository
                .findByRefreshTokenHash(hash(refreshToken))
                .orElseThrow(() -> new BusinessException(CommonErrorCode.AUTH_TOKEN_INVALID));
        if (!previous.isActive()) {
            throw new BusinessException(CommonErrorCode.AUTH_TOKEN_EXPIRED);
        }
        previous.revoke("REFRESH_TOKEN_ROTATED");
        return create(
                previous.getUser(), previous.getDeviceName(), previous.getDeviceType(), previous.getIpAddress(), null);
    }

    public void revoke(Long sessionId, String reason) {
        if (sessionId == null) {
            return;
        }
        repository.findById(sessionId).ifPresent(session -> session.revoke(reason));
    }

    public void revokeAllForUser(Long userId, String reason) {
        repository.findByUserIdOrderByLoginAtDesc(userId).forEach(session -> session.revoke(reason));
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listByUser(Long userId) {
        return repository.findByUserIdOrderByLoginAtDesc(userId).stream()
                .map(session -> new SessionResponse(
                        session.getId(),
                        session.getDeviceName(),
                        session.getDeviceType(),
                        session.getIpAddress(),
                        session.getLoginAt(),
                        session.getLastActivityAt(),
                        session.getExpiresAt(),
                        session.getRevokedAt(),
                        session.getRevokedReason()))
                .toList();
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 không khả dụng", exception);
        }
    }

    public record IssuedSession(UserSession session, String refreshToken) {}
}
