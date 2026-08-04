package com.equipmentrental.events;

import java.math.BigDecimal;

public record DamageCompensationCalculatedEvent(
        BaseEvent metadata,
        Long rentalId,
        Long equipmentId,
        BigDecimal compensationAmount
) {
}
