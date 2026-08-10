package com.equipmentrental.events;

import java.time.Instant;

public record EquipmentReturnedEvent(BaseEvent metadata, Long rentalId, Long equipmentId, Instant returnedAt) {}
