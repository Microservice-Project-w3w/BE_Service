package com.equipmentrental.events;

import java.time.Instant;

public record EquipmentReservedEvent(
        BaseEvent metadata,
        Long rentalId,
        Long equipmentId,
        Instant reservedUntil
) {
}
