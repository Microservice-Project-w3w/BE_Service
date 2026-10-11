package com.equipmentrental.inventory.repository;
import com.equipmentrental.inventory.entity.DemoInventoryOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DemoInventoryOperationRepository extends JpaRepository<DemoInventoryOperation,Long> {
    Optional<DemoInventoryOperation> findByRequestKey(String key);
    Optional<DemoInventoryOperation> findByRentalOrderIdAndKind(Long orderId,String kind);
}
