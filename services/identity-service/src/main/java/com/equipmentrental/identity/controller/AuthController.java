package com.equipmentrental.identity.controller;

import com.equipmentrental.identity.dto.auth.AuthResponse;
import com.equipmentrental.identity.dto.auth.LoginRequest;
import com.equipmentrental.identity.dto.auth.RegisterRequest;
import com.equipmentrental.identity.dto.auth.RegisterResponse;
import com.equipmentrental.identity.dto.auth.RefreshTokenRequest;
import com.equipmentrental.identity.dto.auth.VerificationCodeRequest;
import com.equipmentrental.identity.dto.auth.VerifyEmailRequest;
import com.equipmentrental.identity.dto.auth.ChangePasswordRequest;
import com.equipmentrental.identity.dto.auth.ResetPasswordRequest;
import com.equipmentrental.identity.dto.auth.ConfirmResetPasswordRequest;
import com.equipmentrental.identity.service.AuthService;
import com.equipmentrental.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đăng ký thành công"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Device-Name", required = false) String deviceName,
            @RequestHeader(value = "X-Device-Type", required = false) String deviceType,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            jakarta.servlet.http.HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request, deviceName, deviceType,
                httpRequest.getRemoteAddr(), userAgent), "Đăng nhập thành công"));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(JwtAuthenticationToken authentication) {
        authService.logout(authentication.getToken());
        return ApiResponse.success(null, "Đăng xuất thành công");
    }

    @PostMapping("/verification-codes")
    public ApiResponse<Map<String, String>> requestVerificationCode(@Valid @RequestBody VerificationCodeRequest request) {
        String code = authService.requestEmailVerification(request.email());
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Nếu email tồn tại, mã xác minh đã được tạo.");
        if (code != null) {
            response.put("verificationCode", code);
        }
        return ApiResponse.success(response);
    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.email(), request.code());
        return ApiResponse.success(null, "Xác minh Gmail thành công");
    }

    @PostMapping("/password-reset")
    public ApiResponse<Map<String, String>> requestPasswordReset(@Valid @RequestBody ResetPasswordRequest request) {
        String code = authService.requestPasswordReset(request.email());
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Nếu email tồn tại, mã đặt lại mật khẩu đã được tạo.");
        if (code != null) {
            response.put("verificationCode", code);
        }
        return ApiResponse.success(response);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody ConfirmResetPasswordRequest request
    ){
        authService.resetPassword(
                request.email(),
                request.newPassword()
        );

        return ApiResponse.success(
                null,
                "Đặt lại mật khẩu thành công"
        );
    }
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(JwtAuthenticationToken authentication,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        Jwt jwt = authentication.getToken();
        authService.changePassword(Long.valueOf(jwt.getSubject()), request.currentPassword(), request.newPassword());
        return ApiResponse.success(null, "Đổi mật khẩu thành công");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            JwtAuthenticationToken authentication
    ) {
        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "email",
                authentication
                        .getToken()
                        .getClaimAsString("preferred_username")
        );

        response.put(
                "userId",
                authentication.getToken().getSubject()
        );

        response.put(
                "roles",
                authentication
                        .getToken()
                        .getClaim("roles")
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
