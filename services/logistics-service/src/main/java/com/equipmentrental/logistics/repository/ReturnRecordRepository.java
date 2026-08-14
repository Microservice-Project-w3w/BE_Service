package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.ReturnRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRecordRepository extends JpaRepository<ReturnRecord, Long> {
    Optional<ReturnRecord> findByReturnRequestId(Long returnRequestId);

    Optional<ReturnRecord> findByRentalOrderId(Long rentalOrderId);
}
