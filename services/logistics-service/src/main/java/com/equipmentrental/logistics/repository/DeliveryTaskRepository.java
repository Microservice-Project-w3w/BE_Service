package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.DeliveryTask;
import com.equipmentrental.logistics.entity.enums.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {
    List<DeliveryTask> findByDeliveryStaffUserId(Long deliveryStaffUserId);

    List<DeliveryTask> findByDeliveryStaffUserIdAndStatus(Long deliveryStaffUserId, TaskStatus status);

    Optional<DeliveryTask> findByRentalOrderId(Long rentalOrderId);

    List<DeliveryTask> findByScheduledAtGreaterThanEqualAndScheduledAtLessThan(LocalDateTime start, LocalDateTime end);
}
