package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.ReturnRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Optional<ReturnRequest> findByRentalOrderId(Long rentalOrderId);

    List<ReturnRequest> findByCustomerId(Long customerId);
}
