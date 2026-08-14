package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    List<RentalOrder> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    List<RentalOrder> findByOrganizationIdAndBranchIdAndCustomerId(
            Long organizationId, Long branchId, Long customerId);

    List<RentalOrder> findByStatusAndReservedUntilBefore(OrderStatus status, LocalDateTime time);
}
