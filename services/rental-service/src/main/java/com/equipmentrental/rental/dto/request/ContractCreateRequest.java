package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.NotNull;

public record ContractCreateRequest(@NotNull Long rentalOrderId, String terms) {}
