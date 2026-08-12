package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.DispatchNote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DispatchNoteRepository extends JpaRepository<DispatchNote, Long> {
    Optional<DispatchNote> findByRentalOrderId(Long rentalOrderId);

    Optional<DispatchNote> findByDeliveryTaskId(Long deliveryTaskId);

    List<DispatchNote> findByCustomerId(Long customerId);
}
