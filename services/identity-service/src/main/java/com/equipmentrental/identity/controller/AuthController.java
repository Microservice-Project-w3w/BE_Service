package com.equipmentrental.identity.controller;

import com.equipmentrental.identity.dto.auth.AuthResponse;
import com.equipmentrental.identity.dto.auth.LoginRequest;
import com.equipmentrental.identity.dto.auth.RegisterRequest;
import com.equipmentrental.identity.dto.auth.RegisterResponse;
import com.equipmentrental.identity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            JwtAuthenticationToken authentication
    ) {
        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "email",
                authentication.getName()
        );

        response.put(
                "userId",
                authentication
                        .getToken()
                        .getClaim("userId")
        );

        response.put(
                "roles",
                authentication
                        .getToken()
                        .getClaim("roles")
        );

        return ResponseEntity.ok(response);
    }
}
