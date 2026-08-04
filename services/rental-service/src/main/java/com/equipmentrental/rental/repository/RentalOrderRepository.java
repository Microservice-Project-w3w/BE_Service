package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    List<RentalOrder> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    List<RentalOrder> findByStatusAndReservedUntilBefore(OrderStatus status, LocalDateTime time);
}
