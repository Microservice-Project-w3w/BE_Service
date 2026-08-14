package com.equipmentrental.identity.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Gmail không được để trống") @Email(message = "Gmail không đúng định dạng") String email,
        @NotBlank(message = "Mật khẩu không được để trống") String password) {}
