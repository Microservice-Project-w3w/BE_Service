package com.equipmentrental.inventory.repository;
import com.equipmentrental.inventory.entity.DemoMaintenanceTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DemoMaintenanceTicketRepository extends JpaRepository<DemoMaintenanceTicket,Long> {
    @org.springframework.data.jpa.repository.Query("select t.equipmentId from DemoMaintenanceTicket t where t.id=:id")
    java.util.Optional<Long> findEquipmentId(@org.springframework.data.repository.query.Param("id") Long id);
    List<DemoMaintenanceTicket> findByOrganizationIdAndBranchIdOrderByScheduledAtAsc(Long org,Long branch);
    boolean existsByEquipmentIdAndStatusIn(Long equipmentId,List<String> statuses);
}
