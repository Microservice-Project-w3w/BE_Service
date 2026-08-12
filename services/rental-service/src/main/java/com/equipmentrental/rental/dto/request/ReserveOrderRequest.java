package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record ReserveOrderRequest(@NotNull LocalDateTime reservedUntil) {}
