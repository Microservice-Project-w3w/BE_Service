package com.equipmentrental.events;

import java.time.Instant;
import java.util.UUID;

public record BaseEvent(UUID eventId, Instant occurredAt, String source) {}
