package com.equipmentrental.rental.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record RentalRequestUpdate(
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        String deliveryAddress,
        String note,
        @NotEmpty List<@Valid RentalRequestItemRequest> items) {}
