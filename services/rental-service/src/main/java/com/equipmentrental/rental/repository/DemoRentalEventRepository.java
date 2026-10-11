package com.equipmentrental.rental.repository;
import com.equipmentrental.rental.entity.DemoRentalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DemoRentalEventRepository extends JpaRepository<DemoRentalEvent, Long> {
    List<DemoRentalEvent> findByRentalOrderIdOrderByIdAsc(Long rentalOrderId);
    Optional<DemoRentalEvent> findByRentalOrderIdAndRequestKey(Long rentalOrderId, String requestKey);
}
