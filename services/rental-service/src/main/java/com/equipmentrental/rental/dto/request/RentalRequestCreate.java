package com.equipmentrental.rental.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record RentalRequestCreate(
        @NotNull Long organizationId,
        @NotNull Long branchId,
        @NotNull Long customerId,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        String deliveryAddress,
        String note,
        @NotEmpty List<@Valid RentalRequestItemRequest> items) {}
