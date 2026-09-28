package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.*;

public record RentalRequestItemRequest(@NotNull Long equipmentTypeId, @NotNull @Min(1) Integer quantity) {}
