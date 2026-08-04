package com.equipmentrental.identity.service;

import com.equipmentrental.identity.dto.auth.AuthResponse;
import com.equipmentrental.identity.dto.auth.LoginRequest;
import com.equipmentrental.identity.dto.auth.RegisterRequest;
import com.equipmentrental.identity.dto.auth.RegisterResponse;
import com.equipmentrental.identity.entity.Role;
import com.equipmentrental.identity.entity.User;
import com.equipmentrental.identity.entity.UserStatus;
import com.equipmentrental.identity.entity.PasswordHistory;
import com.equipmentrental.identity.repository.PasswordHistoryRepository;
import com.equipmentrental.identity.repository.RoleRepository;
import com.equipmentrental.identity.repository.UserRepository;
import com.equipmentrental.identity.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class AuthService {

    private static final String CUSTOMER_ROLE = "CUSTOMER";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final VerificationService verificationService;
    private final PasswordHistoryRepository passwordHistoryRepository;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SessionService sessionService,
            VerificationService verificationService,
            PasswordHistoryRepository passwordHistoryRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.verificationService = verificationService;
        this.passwordHistoryRepository = passwordHistoryRepository;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (!normalizedEmail.endsWith("@gmail.com")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hệ thống chỉ chấp nhận địa chỉ Gmail"
            );
        }

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Gmail đã được sử dụng"
            );
        }

        Role customerRole = roleRepository
                .findByCode(CUSTOMER_ROLE)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Chưa cấu hình vai trò CUSTOMER"
                        )
                );

        if (!customerRole.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Vai trò CUSTOMER đang bị vô hiệu hóa"
            );
        }

        User user = new User();

        user.setRole(customerRole);
        user.setFullName(request.fullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        /*
         * Tài khoản chưa được đăng nhập cho tới khi xác minh Gmail.
         */
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);
        user.setFailedLoginAttempts(0);

        /*
         * Khách hàng tự đăng ký nên created_by = NULL.
         */
        user.setCreatedBy(null);
        user.setUpdatedBy(null);

        User savedUser = userRepository.save(user);
        passwordHistoryRepository.save(new PasswordHistory(savedUser, savedUser.getPasswordHash(), "REGISTER"));

        String verificationCode = verificationService.issue(savedUser, VerificationService.PURPOSE_VERIFY_EMAIL);
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getStatus().name(),
                "Đăng ký thành công. Vui lòng xác minh Gmail.",
                verificationCode
        );
    }

    public AuthResponse login(LoginRequest request, String deviceName, String deviceType, String ipAddress, String userAgent) {
        String normalizedEmail = normalizeEmail(request.email());

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Gmail hoặc mật khẩu không chính xác"
                        )
                );

        unlockAccountWhenExpired(user);

        if (user.getStatus() == UserStatus.PENDING
                || !user.isEmailVerified()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Gmail chưa được xác minh"
            );
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new ResponseStatusException(
                    HttpStatus.LOCKED,
                    "Tài khoản đang bị khóa"
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tài khoản không hoạt động"
            );
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            handleFailedLogin(user);

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Gmail hoặc mật khẩu không chính xác"
            );
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());

        userRepository.save(user);

        return issueTokens(user, sessionService.create(user, deviceName, deviceType, ipAddress, userAgent));
    }

    public AuthResponse refresh(String refreshToken) {
        SessionService.IssuedSession issued = sessionService.rotate(refreshToken);
        User user = userRepository.findDetailedById(issued.session().getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại"));
        if (user.getStatus() != UserStatus.ACTIVE || !user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản không hoạt động");
        }
        return issueTokens(user, issued);
    }

    public void logout(Jwt jwt) {
        if (jwt == null) {
            return;
        }
        String sessionId = jwt.getClaimAsString("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        try {
            sessionService.revoke(Long.valueOf(sessionId), "LOGOUT");
        } catch (NumberFormatException ignored) {
            // Older access tokens without a session id are stateless and simply expire.
        }
    }

    public void verifyEmail(String email, String code) {
        User user = verificationService.verify(normalizeEmail(email), VerificationService.PURPOSE_VERIFY_EMAIL, code);
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    public String requestPasswordReset(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .map(user -> verificationService.issue(user, VerificationService.PURPOSE_RESET_PASSWORD))
                .orElse(null);
    }

    public String requestEmailVerification(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .map(user -> verificationService.issue(user, VerificationService.PURPOSE_VERIFY_EMAIL))
                .orElse(null);
    }

    public void resetPassword(String email, String code, String newPassword) {
        User user = verificationService.verify(normalizeEmail(email), VerificationService.PURPOSE_RESET_PASSWORD, code);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        if (user.isEmailVerified()) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
        passwordHistoryRepository.save(new PasswordHistory(user, user.getPasswordHash(), "RESET_PASSWORD"));
        sessionService.revokeAllForUser(user.getId(), "PASSWORD_RESET");
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findDetailedById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không chính xác");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordHistoryRepository.save(new PasswordHistory(user, user.getPasswordHash(), "USER_CHANGE"));
        sessionService.revokeAllForUser(user.getId(), "PASSWORD_CHANGED");
    }

    private AuthResponse issueTokens(User user, SessionService.IssuedSession issued) {
        return new AuthResponse(jwtService.generateAccessToken(user, issued.session().getId()), "Bearer",
                jwtService.getExpiresInSeconds(), issued.refreshToken(), user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().getCode());
    }

    private void handleFailedLogin(User user) {
        int failedAttempts =
                user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(failedAttempts);

        if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(
                    LocalDateTime.now()
                            .plusMinutes(LOCK_MINUTES)
            );
        }

        userRepository.save(user);
    }

    private void unlockAccountWhenExpired(User user) {
        if (user.getStatus() != UserStatus.LOCKED) {
            return;
        }

        LocalDateTime lockedUntil = user.getLockedUntil();

        if (lockedUntil != null
                && LocalDateTime.now().isAfter(lockedUntil)) {

            user.setStatus(UserStatus.ACTIVE);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);

            userRepository.save(user);
        }
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
