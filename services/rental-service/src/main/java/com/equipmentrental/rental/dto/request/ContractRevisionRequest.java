package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContractRevisionRequest(@NotBlank @Size(max = 2000) String terms) {}
