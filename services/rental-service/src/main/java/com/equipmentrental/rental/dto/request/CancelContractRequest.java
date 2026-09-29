package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancelContractRequest(@NotBlank String reason) {}
