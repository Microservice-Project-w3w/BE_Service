package com.equipmentrental.events;

public record EquipmentReleasedEvent(BaseEvent metadata, Long rentalId, Long equipmentId, String reason) {}
