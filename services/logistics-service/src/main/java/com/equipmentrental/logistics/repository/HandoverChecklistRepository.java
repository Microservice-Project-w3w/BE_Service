package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.HandoverChecklist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HandoverChecklistRepository extends JpaRepository<HandoverChecklist, Long> {
    List<HandoverChecklist> findByHandoverRecordId(Long handoverRecordId);
}
