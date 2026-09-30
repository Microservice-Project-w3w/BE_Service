package com.equipmentrental.identity.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record   ConfirmResetPasswordRequest(@NotBlank @Email String email,
                                            @NotBlank
                                            @Size(min = 8, max = 72)
                                            String newPassword) {
}
