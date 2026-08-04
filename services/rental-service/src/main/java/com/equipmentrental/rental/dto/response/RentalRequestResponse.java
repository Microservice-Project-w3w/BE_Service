package com.equipmentrental.rental.dto.response;

import com.equipmentrental.rental.entity.RequestStatus;
import java.time.LocalDateTime;
import java.util.List;

public record RentalRequestResponse(Long id, Long organizationId, Long branchId, String requestCode, Long customerId,
                                    LocalDateTime startAt, LocalDateTime endAt, String deliveryAddress, String note,
                                    RequestStatus status, List<RentalRequestItemResponse> items,
                                    LocalDateTime createdAt) {
}
