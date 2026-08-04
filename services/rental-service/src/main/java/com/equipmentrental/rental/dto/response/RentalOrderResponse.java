package com.equipmentrental.rental.dto.response;

import com.equipmentrental.rental.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalOrderResponse(Long id, Long organizationId, Long branchId, String orderCode, Long quotationId,
                                  Long customerId, LocalDateTime startAt, LocalDateTime endAt,
                                  BigDecimal totalAmount, OrderStatus status, LocalDateTime reservedUntil,
                                  String inventoryReservationId, String cancelReason) {
}
