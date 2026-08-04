package com.equipmentrental.events;

import java.math.BigDecimal;

public record DepositPaidEvent(
        BaseEvent metadata,
        Long rentalId,
        Long paymentId,
        BigDecimal amount
) {
}
