package com.equipmentrental.identity.service;

import com.equipmentrental.identity.dto.auth.AuthResponse;
import com.equipmentrental.identity.dto.auth.LoginRequest;
import com.equipmentrental.identity.dto.auth.RegisterRequest;
import com.equipmentrental.identity.dto.auth.RegisterResponse;
import com.equipmentrental.identity.entity.Role;
import com.equipmentrental.identity.entity.User;
import com.equipmentrental.identity.entity.UserStatus;
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

@Service
public class AuthService {

    private static final String CUSTOMER_ROLE = "CUSTOMER";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getStatus().name(),
                "Đăng ký thành công. Vui lòng xác minh Gmail."
        );
    }

    public AuthResponse login(LoginRequest request) {
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

        String accessToken =
                jwtService.generateAccessToken(user);

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtService.getExpiresInSeconds(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getCode()
        );
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
