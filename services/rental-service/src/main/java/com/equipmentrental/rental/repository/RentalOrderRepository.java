package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.OrderStatus;
import com.equipmentrental.rental.entity.RentalOrder;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from RentalOrder e where e.id = :id")
    java.util.Optional<RentalOrder> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    List<RentalOrder> findByOrganizationIdAndBranchId(Long organizationId, Long branchId);

    List<RentalOrder> findByOrganizationIdAndBranchIdAndCustomerId(
            Long organizationId, Long branchId, Long customerId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    List<RentalOrder> findByStatusAndReservedUntilBefore(OrderStatus status, LocalDateTime time);
}
