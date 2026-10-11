package com.equipmentrental.rental.repository;
import com.equipmentrental.rental.entity.DemoSyncTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DemoSyncTaskRepository extends JpaRepository<DemoSyncTask,String> {
    List<DemoSyncTask> findByRentalOrderIdOrderByCreatedAtAsc(Long orderId);
    boolean existsByRentalOrderIdAndStatus(Long orderId,String status);
}
