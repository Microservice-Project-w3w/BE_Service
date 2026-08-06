package com.equipmentrental.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionRequest(@NotBlank @Size(max = 150) String code, @NotBlank @Size(max = 150) String name,
                                @NotBlank @Size(max = 50) String domain, @Size(max = 500) String description) {
}
