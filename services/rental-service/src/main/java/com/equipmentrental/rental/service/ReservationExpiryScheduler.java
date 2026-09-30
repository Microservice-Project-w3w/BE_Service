package com.equipmentrental.rental.service;

import com.equipmentrental.rental.entity.OrderStatus;
import com.equipmentrental.rental.entity.RentalOrder;
import com.equipmentrental.rental.repository.RentalOrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReservationExpiryScheduler {
    private final RentalOrderRepository orders;

    public ReservationExpiryScheduler(RentalOrderRepository orders) {
        this.orders = orders;
    }

    @Scheduled(fixedDelayString = "${app.reservation-expiry-check-ms:60000}")
    @Transactional
    public void releaseExpiredOrderReservations() {
        List<RentalOrder> expiredOrders = orders.findByStatusAndReservedUntilBefore(OrderStatus.RESERVED, LocalDateTime.now());
        expiredOrders.forEach(order -> {
            order.setStatus(OrderStatus.PENDING);
            order.setInventoryReservationId(null);
            order.setReservedUntil(null);
        });
    }
}
