package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record ReserveOrderRequest(
        @NotNull LocalDateTime reservedUntil,
        @NotEmpty List<@NotNull Long> equipmentIds) {}
