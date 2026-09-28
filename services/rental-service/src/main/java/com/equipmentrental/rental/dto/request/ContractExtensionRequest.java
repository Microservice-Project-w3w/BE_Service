package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ContractExtensionRequest(@NotNull LocalDateTime newEndAt, @NotBlank String terms) {}
