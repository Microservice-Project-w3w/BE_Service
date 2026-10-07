package com.equipmentrental.inventory.dto.request;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
public record ExtendReservationRequest(@NotNull LocalDateTime newEndAt) {}
