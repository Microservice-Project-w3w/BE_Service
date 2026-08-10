package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.HandoverRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HandoverRecordRepository extends JpaRepository<HandoverRecord, Long> {
    Optional<HandoverRecord> findByDispatchNoteId(Long dispatchNoteId);
}
