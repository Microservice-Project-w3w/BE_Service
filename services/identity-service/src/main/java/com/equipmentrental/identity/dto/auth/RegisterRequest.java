package com.equipmentrental.identity.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Họ tên không được để trống")
                @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự")
                String fullName,
        @NotBlank(message = "Gmail không được để trống")
                @Email(message = "Gmail không đúng định dạng")
                @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Hệ thống chỉ chấp nhận địa chỉ Gmail")
                String email,
        @NotBlank(message = "Mật khẩu không được để trống")
                @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự")
                String password) {}
