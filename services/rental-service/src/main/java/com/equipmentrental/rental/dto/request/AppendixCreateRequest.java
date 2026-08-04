package com.equipmentrental.rental.dto.request;

import com.equipmentrental.rental.entity.AppendixType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AppendixCreateRequest(@NotNull AppendixType appendixType, LocalDateTime newEndAt,
                                    @NotBlank String terms) {
}
