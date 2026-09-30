package com.equipmentrental.inventory.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpiryScheduler {
    private final InternalReservationService reservations;

    public ReservationExpiryScheduler(InternalReservationService reservations) {
        this.reservations = reservations;
    }

    @Scheduled(fixedDelayString = "${app.reservation-expiry-check-ms:60000}")
    public void expireOverdueReservations() {
        reservations.expireOverdueReservations();
    }
}
