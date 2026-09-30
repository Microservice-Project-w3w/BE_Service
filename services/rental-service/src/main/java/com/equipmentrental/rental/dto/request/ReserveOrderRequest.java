package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record ReserveOrderRequest(
        @NotNull LocalDateTime reservedUntil,
        List<Long> equipmentIds) {}
